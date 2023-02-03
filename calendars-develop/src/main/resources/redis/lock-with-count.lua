-----------------
-- Locks a key and, if lock is obtained, sets the lock count.
-----------------
local currentKey, countKey = KEYS[1], KEYS[2]
local lock, count, ex = ARGV[1], ARGV[2], ARGV[3]

-- Set if not exists, and get prev value. Once we're on redis 7, this can be done in one call:
-- local prev = redis.call('SET', currentKey, lock, 'NX', 'EX', ex, 'GET')
local prev = nil
if redis.call('SET', currentKey, lock, 'NX', 'EX', ex) ~= nil then
  prev = redis.call('GET', currentKey)
end

-- If lock was same as existing, just set new TTL.
if prev == lock then
  redis.call('EXPIRE', currentKey, ex)
end

-- If lock was same or set as new, set the lock count and return true to indicate lock obtained.
if prev == lock or not prev then
  redis.call('SET', countKey, count, 'EX', ex)
  return true
end

return false
