package cloudsim.ext.datacenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;

/**
 * Energy-aware load balancer using Power-Aware Best Fit strategy.
 * Consolidates workloads onto fewer VMs to reduce the number of active hosts,
 * since idle hosts consume ~75% of peak power (Beloglazov et al., 2012).
 *
 * Algorithm: Among VMs below a load ceiling, select the most loaded one
 * (bin-packing). Falls back to least-loaded if all are over ceiling (QoS safety).
 */
public class GreenVmLoadBalancer extends VmLoadBalancer implements CloudSimEventListener {

	private Map<Integer, VirtualMachineState> vmStatesList;
	private Map<Integer, Integer> currentLoad;
	private static final int MAX_LOAD_PER_VM = 10;

	public GreenVmLoadBalancer(DatacenterController dcb) {
		super();
		this.vmStatesList = dcb.getVmStatesList();
		this.currentLoad = Collections.synchronizedMap(new HashMap<Integer, Integer>());
		dcb.addCloudSimEventListener(this);
	}

	@Override
	public int getNextAvailableVm() {
		if (vmStatesList.isEmpty()) return -1;

		int bestVm = -1;
		int bestLoad = -1;
		int leastLoadedVm = -1;
		int leastLoad = Integer.MAX_VALUE;

		for (int vmId : vmStatesList.keySet()) {
			Integer load = currentLoad.get(vmId);
			int loadVal = (load == null) ? 0 : load;

			// Track least loaded for fallback
			if (loadVal < leastLoad) {
				leastLoad = loadVal;
				leastLoadedVm = vmId;
			}

			// Best fit: highest load still under ceiling
			if (loadVal < MAX_LOAD_PER_VM && loadVal > bestLoad) {
				bestLoad = loadVal;
				bestVm = vmId;
			}
		}

		// If all above ceiling, use least loaded (QoS safety valve)
		if (bestVm == -1) bestVm = leastLoadedVm;
		if (bestVm == -1) bestVm = 0;

		allocatedVm(bestVm);
		return bestVm;
	}

	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM) {
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			Integer curr = currentLoad.get(vmId);
			currentLoad.put(vmId, (curr == null) ? 1 : curr + 1);
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET) {
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			Integer curr = currentLoad.get(vmId);
			currentLoad.put(vmId, (curr == null || curr <= 0) ? 0 : curr - 1);
		}
	}
}
