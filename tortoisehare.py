def floyd(nums):
    t = nums[0]
    h = nums[0]
    while True:
        t = nums[t]
        h = nums[nums[h]]
        if t == h:
            break
    p1 = nums[0]
    p2 = t
    while p1 != p2:
        p1 = nums[p1]
        p2 = nums[p2]
    return p1


nums = [7, 0, 4, 9, 6, 10, 8, 1, 2, 5, 3]
print(floyd(nums))