package cloudsim.ext.datacenter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import cloudsim.ext.Constants;
import cloudsim.ext.event.CloudSimEvent;
import cloudsim.ext.event.CloudSimEventListener;
import cloudsim.ext.event.CloudSimEvents;


public class WeightedRoundRobinVmLoadBalancer extends VmLoadBalancer implements CloudSimEventListener  {

	private Map<Integer, VirtualMachineState> vmStatesList;
	private int[] vmWeights;
	private static final int DEFAULT_WEIGHT = 20;

	public WeightedRoundRobinVmLoadBalancer(Map<Integer, VirtualMachineState> vmStatesList, DatacenterController dcb){
		super();
		dcb.addCloudSimEventListener(this);
		this.vmStatesList = vmStatesList;

		int vmCount = Math.max(1, vmStatesList.size());
		vmWeights = new int[vmCount];
		for (int i = 0; i < vmCount; i++) {
			vmWeights[i] = DEFAULT_WEIGHT;
		}
	}

	public int getNextAvailableVm(){
		int vm = weightedRoundRobin();
		allocatedVm(vm);
		return vm;
	}

	public int weightedRoundRobin() {
		int i = -1;
		int cw = 0;
		int len = vmWeights.length;

		while (true) {
			i = (i + 1) % len;
			if (i == 0) {
				cw = cw - vmGCD(vmWeights);
				if (cw <= 0) {
					cw = vmMax(vmWeights);
					if (cw == 0) return 0;
				}
			}
			if (vmWeights[i] >= cw) return i;
		}
	}

	public int vmGCD(int[] vmWeights) {
		int currGcd = gcd(vmWeights[0], vmWeights.length > 1 ? vmWeights[1] : vmWeights[0]);
		for (int i = 2; i < vmWeights.length; i++) {
			currGcd = gcd(currGcd, vmWeights[i]);
		}
		return currGcd;
	}

	public int vmMax(int[] vmWeights) {
		int max = -1;
		for(int wt : vmWeights) {
			if(wt > max) max = wt;
		}
		return max;
	}

	static int gcd(int a, int b)
    {
        if (a == 0) return b;
        if (b == 0) return a;
        if (a == b) return a;
        if (a > b) return gcd(a-b, b);
        return gcd(a, b-a);
    }

	public void cloudSimEventFired(CloudSimEvent e) {
		if (e.getId() == CloudSimEvents.EVENT_CLOUDLET_ALLOCATED_TO_VM){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			if (vmId >= 0 && vmId < vmWeights.length) {
				vmWeights[vmId]--;
			}
		} else if (e.getId() == CloudSimEvents.EVENT_VM_FINISHED_CLOUDLET){
			int vmId = (Integer) e.getParameter(Constants.PARAM_VM_ID);
			if (vmId >= 0 && vmId < vmWeights.length) {
				vmWeights[vmId]++;
			}
		}
	}
}
