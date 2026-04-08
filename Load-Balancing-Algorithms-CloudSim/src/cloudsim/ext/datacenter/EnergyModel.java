package cloudsim.ext.datacenter;

import cloudsim.ext.Constants;

/**
 * Energy consumption model for cloud data centers.
 * Uses a linear power model: P(u) = P_idle + (P_max - P_idle) * u
 * Based on Beloglazov et al. (2012) and SPECpower benchmark data.
 */
public class EnergyModel implements Constants {

	private double hostIdlePowerW;
	private double hostMaxPowerW;
	private double pue;

	public EnergyModel(double idlePower, double maxPower, double pue) {
		this.hostIdlePowerW = idlePower;
		this.hostMaxPowerW = maxPower;
		this.pue = pue;
	}

	/** Power consumption in Watts at given CPU utilization (0.0 to 1.0). */
	public double getPower(double utilization) {
		utilization = Math.max(0.0, Math.min(1.0, utilization));
		return hostIdlePowerW + (hostMaxPowerW - hostIdlePowerW) * utilization;
	}

	/** Energy consumed in kWh for a host running durationMs milliseconds at given utilization. */
	public double getEnergyKWh(double utilization, double durationMs) {
		double powerW = getPower(utilization);
		double hours = durationMs / MILLI_SECONDS_TO_HOURS;
		return (powerW * hours * pue) / 1000.0;
	}

	/** Carbon footprint in kgCO2 for given energy consumption in a specific region. */
	public double getCarbonKg(double energyKWh, int region) {
		if (region < 0 || region >= CARBON_INTENSITY_BY_REGION.length) {
			region = 0;
		}
		return energyKWh * CARBON_INTENSITY_BY_REGION[region] / 1000.0;
	}

	/**
	 * Compute a composite Green Score (higher = greener).
	 * Balances energy efficiency (40%), carbon efficiency (40%), and
	 * performance (20%) into a single comparable metric.
	 */
	public static double computeGreenScore(double totalEnergyKWh, double totalCarbonKg,
										   double avgResponseTimeMs) {
		if (totalEnergyKWh <= 0 && totalCarbonKg <= 0) return 100.0;

		double energyScore = 1000.0 / (1.0 + totalEnergyKWh);
		double carbonScore = 1000.0 / (1.0 + totalCarbonKg);
		double perfScore = 1000.0 / (1.0 + avgResponseTimeMs);

		return (energyScore * 0.4 + carbonScore * 0.4 + perfScore * 0.2);
	}
}
