package com.easy.utils;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 敏感词处理工具类
 * 使用DFA（Deterministic Finite Automaton）算法和Trie树实现高效的敏感词过滤
 */
@Slf4j
@Component
public class SensitiveWordUtil {

    /**
     * 敏感词Trie树根节点
     */
    private TrieNode rootNode;

    /**
     * 敏感词文件路径列表
     */
    private static final List<String> SENSITIVE_WORD_FILES = Arrays.asList(
            "sensitive-word/pub_sms_banned_words.txt",
            "sensitive-word/pub_banned_words.txt"
    );

    /**
     * 无意义字符集合（需要删除的字符）
     */
    private static final Set<Character> MEANINGLESS_CHARS = new HashSet<>(Arrays.asList(
            '\u0000', '\u0001', '\u0002', '\u0003', '\u0004', '\u0005', '\u0006', '\u0007',
            '\u0008', '\u000B', '\u000C', '\u000E', '\u000F', '\u0010', '\u0011', '\u0012',
            '\u0013', '\u0014', '\u0015', '\u0016', '\u0017', '\u0018', '\u0019','\u001A',
            '\u001B', '\u001C', '\u001D', '\u001E', '\u001F',
            '\u3000',   // 中文全角空格
            '\uFEFF', '\uFFFE', '\uFFFF'
    ));

    /**
     * 特殊字符映射表（用于统一处理变体字符）
     */
    private static final Map<Character, Character> SPECIAL_CHAR_MAPPING = new HashMap<>();

    static {
        // 全角转半角
        for (char c = '\uFF01'; c <= '\uFF5E'; c++) {
            SPECIAL_CHAR_MAPPING.put(c, (char) (c - 0xFEE0));
        }
        // 常见特殊字符统一处理
        SPECIAL_CHAR_MAPPING.put('·', '.');
        SPECIAL_CHAR_MAPPING.put('．', '.');
        SPECIAL_CHAR_MAPPING.put('｡', '.');
        SPECIAL_CHAR_MAPPING.put('。', '.');
        SPECIAL_CHAR_MAPPING.put('，', ',');
        SPECIAL_CHAR_MAPPING.put('，', ',');
        SPECIAL_CHAR_MAPPING.put('！', '!');
        SPECIAL_CHAR_MAPPING.put('！', '!');
        SPECIAL_CHAR_MAPPING.put('？', '?');
        SPECIAL_CHAR_MAPPING.put('？', '?');
        SPECIAL_CHAR_MAPPING.put('；', ';');
        SPECIAL_CHAR_MAPPING.put('；', ';');
        SPECIAL_CHAR_MAPPING.put('：', ':');
        SPECIAL_CHAR_MAPPING.put('：', ':');
        SPECIAL_CHAR_MAPPING.put('（', '(');
        SPECIAL_CHAR_MAPPING.put('）', ')');
        SPECIAL_CHAR_MAPPING.put('【', '[');
        SPECIAL_CHAR_MAPPING.put('】', ']');
        SPECIAL_CHAR_MAPPING.put('『', '[');
        SPECIAL_CHAR_MAPPING.put('』', ']');
        SPECIAL_CHAR_MAPPING.put('「', '[');
        SPECIAL_CHAR_MAPPING.put('」', ']');
        SPECIAL_CHAR_MAPPING.put('《', '<');
        SPECIAL_CHAR_MAPPING.put('》', '>');
        SPECIAL_CHAR_MAPPING.put('—', '-');
        SPECIAL_CHAR_MAPPING.put('–', '-');
        SPECIAL_CHAR_MAPPING.put('—', '-');
        SPECIAL_CHAR_MAPPING.put('_', '-');
        SPECIAL_CHAR_MAPPING.put('—', '-');
        SPECIAL_CHAR_MAPPING.put('～', '~');
        SPECIAL_CHAR_MAPPING.put('～', '~');
        SPECIAL_CHAR_MAPPING.put('·', '.');
        SPECIAL_CHAR_MAPPING.put('•', '.');
        SPECIAL_CHAR_MAPPING.put('·', '.');
        SPECIAL_CHAR_MAPPING.put('*', '*');
        SPECIAL_CHAR_MAPPING.put('×', '*');
        SPECIAL_CHAR_MAPPING.put('÷', '/');
        SPECIAL_CHAR_MAPPING.put('\\', '/');
        SPECIAL_CHAR_MAPPING.put('|', '|');
        SPECIAL_CHAR_MAPPING.put('¦', '|');
        SPECIAL_CHAR_MAPPING.put('@', '@');
        SPECIAL_CHAR_MAPPING.put('＠', '@');
        SPECIAL_CHAR_MAPPING.put('#', '#');
        SPECIAL_CHAR_MAPPING.put('＃', '#');
        SPECIAL_CHAR_MAPPING.put('$', '$');
        SPECIAL_CHAR_MAPPING.put('＄', '$');
        SPECIAL_CHAR_MAPPING.put('%', '%');
        SPECIAL_CHAR_MAPPING.put('％', '%');
        SPECIAL_CHAR_MAPPING.put('^', '^');
        SPECIAL_CHAR_MAPPING.put('＆', '&');
        SPECIAL_CHAR_MAPPING.put('&', '&');
        SPECIAL_CHAR_MAPPING.put('(', '(');
        SPECIAL_CHAR_MAPPING.put(')', ')');
        SPECIAL_CHAR_MAPPING.put(')', ')');
        SPECIAL_CHAR_MAPPING.put('{', '{');
        SPECIAL_CHAR_MAPPING.put('}', '}');
        SPECIAL_CHAR_MAPPING.put('｛', '{');
        SPECIAL_CHAR_MAPPING.put('｝', '}');
        SPECIAL_CHAR_MAPPING.put('[', '[');
        SPECIAL_CHAR_MAPPING.put(']', ']');
        SPECIAL_CHAR_MAPPING.put('【', '[');
        SPECIAL_CHAR_MAPPING.put('】', ']');
        SPECIAL_CHAR_MAPPING.put('\'', '\'');
        SPECIAL_CHAR_MAPPING.put('’', '\'');
        SPECIAL_CHAR_MAPPING.put('‘', '\'');
        SPECIAL_CHAR_MAPPING.put('"', '"');
        SPECIAL_CHAR_MAPPING.put('“', '"');
        SPECIAL_CHAR_MAPPING.put('”', '"');
        SPECIAL_CHAR_MAPPING.put('`', '`');
        SPECIAL_CHAR_MAPPING.put('´', '`');
        SPECIAL_CHAR_MAPPING.put('‘', '\'');
        SPECIAL_CHAR_MAPPING.put('’', '\'');
    }

    /**
     * Trie树节点
     */
    @Data
    private static class TrieNode {
        /**
         * 子节点映射
         */
        private final Map<Character, TrieNode> children;

        /**
         * 是否为敏感词结尾
         */
        private boolean isEnd;

        /**
         * 敏感词长度（用于支持最短匹配）
         */
        private int wordLength;

        public TrieNode() {
            this.children = new HashMap<>();
            this.isEnd = false;
            this.wordLength = 0;
        }


    }

    /**
     * 初始化敏感词库
     */
    @PostConstruct
    public void init() {
        rootNode = new TrieNode();
        Set<String> sensitiveWords = loadSensitiveWords();
        buildTrieTree(sensitiveWords);
        log.info("敏感词库初始化完成，共加载 {} 个敏感词", sensitiveWords.size());
    }

    /**
     * 加载敏感词文件
     *
     * @return 敏感词集合
     */
    private Set<String> loadSensitiveWords() {
        Set<String> sensitiveWords = new HashSet<>();

        for (String filePath : SENSITIVE_WORD_FILES) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filePath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (!line.isEmpty()) {
                        // 预处理敏感词
                        String processedWord = preprocess(line);
                        if (!processedWord.isEmpty()) {
                            sensitiveWords.add(processedWord);
                        }
                    }
                }
                log.debug("成功加载敏感词文件: {}, 加载词数: {}", filePath, sensitiveWords.size());

            } catch (Exception e) {
                log.warn("加载敏感词文件失败: {}", filePath, e);
            }
        }

        return sensitiveWords;
    }

    /**
     * 构建Trie树
     *
     * @param sensitiveWords 敏感词集合
     */
    private void buildTrieTree(Set<String> sensitiveWords) {
        for (String word : sensitiveWords) {
            TrieNode currentNode = rootNode;
            char[] chars = word.toCharArray();

            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                // 获取当前节点的子节点
                TrieNode child = currentNode.getChildren().get(c);

                // 若当前节点的子节点集合中无该元素（c），则将当前元素加入到子节点集合中
                if (child == null) {
                    child = new TrieNode();
                    currentNode.getChildren().put(c, child);
                }

                // 若是最后一个字符，标记为敏感词结尾
                if (i == chars.length - 1) {
                    child.setEnd(true);
                    child.setWordLength(chars.length);
                }

                // 继续构建下一个元素
                currentNode = child;
            }
        }
    }

    /**
     * 字符预处理
     * 1. 删除无意义字符
     * 2. 统一特殊字符
     * 3. 去除首尾空白字符
     *
     * @param text 原始文本
     * @return 预处理后的文本
     */
    public String preprocess(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        char[] chars = text.toCharArray();

        for (char c : chars) {
            // 删除无意义字符
            if (MEANINGLESS_CHARS.contains(c)) {
                continue;
            }

            // 统一特殊字符
            Character mappedChar = SPECIAL_CHAR_MAPPING.get(c);
            if (mappedChar != null) {
                sb.append(mappedChar);
            } else {
                sb.append(c);
            }
        }

        return sb.toString().trim();
    }

    /**
     * 过滤敏感词，将敏感词替换为 *
     *
     * @param text 原始文本
     * @return 过滤后的文本
     */
    public String filter(String text) {
        return filter(text, '*');
    }

    /**
     * 过滤敏感词，将敏感词替换为指定字符
     *
     * @param text        原始文本
     * @param replaceChar 替换字符
     * @return 过滤后的文本
     */
    public String filter(String text, char replaceChar) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        text = preprocess(text);
        log.info("处理后的字符串：{}", text);

        char[] chars = text.toCharArray();
        StringBuilder result = new StringBuilder(text);

        for (int i = 0; i < chars.length; i++) {
            int matchLength = findSensitiveWord(chars, i);
            if (matchLength > 0) {
                // 将敏感词替换为指定字符
                for (int j = i; j < i + matchLength && j < result.length(); j++) {
                    result.setCharAt(j, replaceChar);
                }
                i += matchLength - 1;
            }
        }

        return result.toString();
    }

    /**
     * 检测文本是否包含敏感词
     *
     * @param text 原始文本
     * @return true:包含敏感词, false:不包含
     */
    public boolean contains(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            if (findSensitiveWord(chars, i) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查找文本中所有的敏感词
     *
     * @param text 原始文本
     * @return 敏感词列表
     */
    public List<String> findAll(String text) {
        List<String> result = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            return result;
        }

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int matchLength = findSensitiveWord(chars, i);
            if (matchLength > 0) {
                String sensitiveWord = new String(chars, i, matchLength);
                if (!result.contains(sensitiveWord)) {
                    result.add(sensitiveWord);
                }
                i += matchLength - 1;
            }
        }

        return result;
    }

    /**
     * 获取文本中第一个敏感词
     *
     * @param text 原始文本
     * @return 第一个敏感词，没有则返回 null
     */
    public String findFirst(String text) {
        if (text == null || text.isEmpty()) {
            return null;
        }

        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int matchLength = findSensitiveWord(chars, i);
            if (matchLength > 0) {
                return new String(chars, i, matchLength);
            }
        }

        return null;
    }

    /**
     * 获取敏感词数量
     *
     * @param text 原始文本
     * @return 敏感词个数
     */
    public int getCount(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int count = 0;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            int matchLength = findSensitiveWord(chars, i);
            if (matchLength > 0) {
                count++;
                i += matchLength - 1;
            }
        }

        return count;
    }

    /**
     * 使用DFA算法查找敏感词
     *
     * @param chars    字符数组
     * @param startPos 起始位置
     * @return 匹配到的敏感词长度，未匹配返回0
     */
    private int findSensitiveWord(char[] chars, int startPos) {
        TrieNode currentNode = rootNode;
        int maxMatchLength = 0;
        int currentMatchLength = 0;

        for (int i = startPos; i < chars.length; i++) {
            char c = chars[i];

            // 检查是否是分隔符（用于跳过无意义字符匹配）
            if (isSeparator(c)) {
                // 如果已经匹配了一部分，继续查找
                if (currentMatchLength > 0) {
                    currentMatchLength++;
                    continue;
                }
                break;
            }

            TrieNode child = currentNode.getChildren().get(c);

            if (child == null) {
                // 如果当前节点是根节点且没有匹配，尝试跳过当前字符
                if (currentNode == rootNode) {
                    continue;
                }
                break;
            }

            currentNode = child;
            currentMatchLength++;

            // 如果到达敏感词结尾，记录匹配长度
            if (child.isEnd()) {
                maxMatchLength = currentMatchLength;
                // 继续查找更长的匹配（最长匹配原则）
            }
        }

        return maxMatchLength;
    }

    /**
     * 判断字符是否为分隔符
     * 分隔符可以出现在敏感词中间而不影响匹配
     *
     * @param c 字符
     * @return true:是分隔符, false:不是
     */
    private boolean isSeparator(char c) {
        return c == '.' || c == '*' || c == '-' || c == '_' || c == '|' || c == '/' || c == '\\';
    }

    /**
     * 重新加载敏感词库
     */
    public void reload() {
        init();
    }

    /**
     * 获取敏感词库大小
     *
     * @return 敏感词数量
     */
    public int getWordCount() {
        return countNodes(rootNode);
    }

    /**
     * 递归统计Trie树中的敏感词数量
     *
     * @param node 当前节点
     * @return 敏感词数量
     */
    private int countNodes(TrieNode node) {
        int count = 0;
        if (node.isEnd()) {
            count++;
        }
        for (TrieNode child : node.getChildren().values()) {
            count += countNodes(child);
        }
        return count;
    }
}
