import time

N = 100000
primes = [2]

def is_prime(x):
  i = 0
  is_prime = True
  while True:
    if primes[i] * primes[i] > x:
        break
    if (x % primes[i] == 0):
        is_prime = False
        break
    i += 1
  return is_prime

def main():
  x = 3
  while True:
    if len(primes) == N:
      break
    if (is_prime(x)):
      primes.append(x)
    x += 1
  return primes[len(primes) - 1]

ITERATION = 100
result_time = []
for i in range(ITERATION):
  start = time.time()
  main()
  end = time.time()
  primes = [2]
  result_time.append(end-start)

print("Python:: time took ==", sum(result_time)/len(result_time))
