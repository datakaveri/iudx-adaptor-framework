# Benchmarking

Commands to run

### Java based JS Engine test

```
mvn package
mvn exec:exec
```

### Python Test

```
python3 test.py
```

### Javascript Test

```
node index.js
```

## Test Result

All of the following are running same prime number generator code

| Engine     | Total Time |
|------------|------------|
| Rhino      | 1835 ms    |
| Nashorn    | 662 ms     |
| GraalVM    | 2154 ms    |

Same code run in plain language scope

| Language | Total Time |
|----------|------------|
| Java     | 54 ms      |
| Python   | 4 seconds  |
| NodeJS   | 46 ms      |

Above result are run on following machine

- CPU: 11th Gen Intel(R) Core(TM) i5-1135G7 @ 2.40GHz 4 core
- RAM: 8GB