# Control Tower

<img width="1384" height="514" alt="Ekran Resmi 2025-09-20 15 20 05" src="https://github.com/user-attachments/assets/1964dd3f-5d50-47d7-8752-f239528faa06" />

### Experiment 1 — Creating Threads (Passenger Arrivals)

In this first experiment, I explore the basics of Java threading:

- **Creating threads** using `Runnable` and `Thread`
- **Naming threads** for clearer debugging
- **Starting threads** with `start()`
- **Joining threads** so the main thread waits until workers finish

**Scenario:**  
Passengers (threads) arrive at the airport and are processed concurrently.  
The control tower (main thread) waits until all passengers are finished before closing the process.
