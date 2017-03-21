# pssim
Content-based publish/subscribe simulator

Input files:
- sim.config
- Topology
- Latencies
- faults (optional)

Build with: 
./install_local_jars.sh
./build.sh

Run with: 
./pssim.sh sim_dir seed log_level (off/error/info)

example: ./pssim.sh src/test/resources/simulations/overlay50/ 53211815 off