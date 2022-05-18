var N = 100000;
var primes = [2];
function is_prime(x) {
  for (var i = 0; primes[i] * primes[i] <= x; ++i) {
    if (x % primes[i] == 0) {
      return false;
    }
  }
  return true;
}
function main() {
  for (var x = 3; primes.length < N; ++x) {
    if (is_prime(x)) {
      primes.push(x);
    }
  }
  return primes[primes.length - 1];
}

const ITERATION = 100;

const resultTime = [];
for(let j=0; j<ITERATION; j++) {
  const start = new Date().getTime()
  main();
  const end = new Date().getTime()
  resultTime.push(end-start);
  primes = [2]
}

const average =resultTime.reduce((a,b) => a+b, 0);
console.log("JS:: time took ==", average/resultTime.length);
