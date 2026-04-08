# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

CloudAnalyst-based simulation comparing load balancing algorithms in a cloud environment. Built on CloudSim/GridSim frameworks with a Swing GUI. Measures server response time and data center processing time across algorithms: Round Robin, Weighted Round Robin, Threshold-Based, Honey Bee Foraging, and Ant Colony Optimization.

## Build & Run

Requires **Java 8** (Temurin JDK 8 configured in `.vscode/settings.json`).

The project uses Apache Ant with a NetBeans project structure. All source is under `Load-Balancing-Algorithms-CloudSim/`.

```bash
# Build
cd Load-Balancing-Algorithms-CloudSim
ant compile

# Run (launches Swing GUI)
ant run
```

The main entry point is `cloudsim.ext.gui.GuiMain.main()`. The NetBeans project.properties references main class `cloud_analyst.Cloud_analyst`, but the actual GUI main is in `GuiMain`.

Dependencies live in `jars/`: simjava2.jar, cloudanalyst.jar, gridsim.jar, iText-2.1.5.jar. These are not managed by Maven/Gradle — they are committed directly.

## Architecture

The code under `Load-Balancing-Algorithms-CloudSim/src/` has three layers:

**Core simulation (`cloudsim/`)** — CloudSim framework classes: `DataCenter`, `VirtualMachine`, `Host`, provisioners, VM schedulers, allocation policies. These are the simulation infrastructure and rarely need modification.

**Extension layer (`cloudsim/ext/`)** — The main application logic:
- `Simulation` — Main controller that wires up data centers, user bases, VMs, and runs the simulation via GridSim. Configurable parameters: load balance policy, service broker policy, grouping factors.
- `DatacenterController` (`ext/datacenter/`) — Manages a single data center; dispatches cloudlets to VMs via a `VmLoadBalancer`. The load balancer is selected by string constant from `Constants`.
- **Load balancers** (`ext/datacenter/`) — Each algorithm extends `VmLoadBalancer` and implements `getNextAvailableVm()`. To add a new algorithm: create a new class extending `VmLoadBalancer`, add a constant in `Constants`, wire it in `DatacenterController`'s constructor switch, and add it to the GUI dropdown in `ConfigureSimulationPanel`.
- **Service brokers** (`ext/servicebroker/`) — Route user requests to data centers (proximity, best response time, dynamic). All extend `CloudAppServiceBroker`.
- `Internet` / `InternetCharacteristics` — Models network delays and bandwidth between 6 world regions using matrices in `resources/`.

**GUI (`cloudsim/ext/gui/`)** — Swing-based. `GuiMain` controls screen transitions between `ConfigureSimulationPanel` (setup), `SimulationPanel` (running), and `ResultsScreen` (output). The GUI fires simulation via `Simulation.runSimulation()`.

**Network topology (`cloudsim/network/`)** — Floyd-Warshall shortest path on BRITE graph topology files.

## Key Constants

`cloudsim/ext/Constants.java` defines all load balance policy names, broker policy names, default DC/VM/UserBase parameters, and simulation tags. Load balancer selection in `DatacenterController` matches against these string constants.

## Git

Do not mention Claude or Anthropic in commit messages, PR descriptions, or any git metadata (no Co-Authored-By lines, etc.).

## Simulation Output

Results write to `sim_report` (detailed SimJava stats) and `sim_trace` at the project root. The GUI also displays results and can export to PDF via `PdfExporter` (uses iText).
