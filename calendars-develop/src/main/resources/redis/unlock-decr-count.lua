-----------------
-- Unlocks a currently locked key by DECR the count and removing the lock when the count <= 0.
-----------------
local currentKey, countKey = KEYS[1], KEYS[2]
local lock = ARGV[1]

local current = redis.call('GET', currentKey)
if current == lock then
  -- Current lock is passed lock, so decrement count and delete when it hits 0.
  local count = redis.call('DECR', countKey)
  if count <= 0 then
    redis.call('DEL', currentKey, countKey)
  end
else
  -- No current lock, or passed lock is not current, so delete countKey.
  redis.call('DEL', countKey)
end
