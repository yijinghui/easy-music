---
--- Created by Hhh
--- DateTime: 2026/7/5 10:01
---
-- KEYS[1]: targetKey (目标榜)
-- KEYS[2]: nowKey (当前活跃榜)

local targetKey = KEYS[1]
local nowKey = KEYS[2]
local count = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

-- 1. 检查nowKey是否存在
if redis.call('EXISTS', nowKey) == 0 then
    return 0 -- 没有数据可归档
end
-- 4. 获取榜单前N名
local members = redis.call('ZREVRANGE', nowKey, 0, count - 1)


-- 2. 归档：使用RENAMENX避免覆盖
if redis.call('RENAMENX', nowKey, targetKey) == 0 then
    -- targetKey已存在，需要处理冲突
    -- 删除旧的targetKey再重命名
    redis.call('DEL', targetKey)
    redis.call('RENAME', nowKey, targetKey)
end

-- 3. 设置周榜过期时间（可选）
redis.call('EXPIRE', targetKey, ttl)

-- 5. 创建新的now榜
if #members > 0 then
    local args = {nowKey}
    for i, member in ipairs(members) do
        table.insert(args, 0)
        table.insert(args, member)
    end
    redis.call('ZADD',  unpack(args))
end

return #members
