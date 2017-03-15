package de.tum.msrg.baseline;

public class ConfigKeys {
	public static String SIM_IS_ADV_BASED = "simulation.adv_based";
	public static String SIM_NO_OF_NODES = "simulation.no_of_nodes";
	public static String SIM_NO_OF_SUBS = "simulation.no_of_subs";
	public static String SIM_NO_OF_ADVS = "simulation.no_of_advs";
	public static String SIM_PUB_DURATION = "simulation.pub_duration";
	public static String SIM_PUB_RATE = "simulation.pub_rate";
	public static String SIM_START_PUBLISH_TIME = "simulation.start_publish_time";
	public static String SIM_DIR = "simulation.dir";
	public static String SIM_MAX_SIM_TIME = "simulation.max_simulation_time";
	// not in conf file but passes as argument
	public static String SIM_RANDOM_SEED = "simulation.seed";
	public static String SIM_APPLY_LOAD_DELAY = "simulation.apply_load_delay";
	
	public static String WLOAD_NO_OF_CLASSES = "workload.no_of_classes";
	public static String WLOAD_NO_OF_ATTRIBUTES = "workload.no_of_attributes";
	public static String WLOAD_PUBLISHER_POPULARITY = "workload.publisher_popularity";
	public static String WLOAD_ZIPF_EXPONENT = "workload.zipf_exponent";
	public static String WLOAD_ATTRIBUTE_MIN_VALUE = "workload.attribute_min_value";
	public static String WLOAD_ATTRIBUTE_MAX_VALUE = "workload.attribute_max_value";
	public static String WLOAD_BIAS_FACTOR = "workload.bias_factor";
	public static String WLOAD_MSG_SIZE = "workload.msg_size";
	public static String WLOAD_ATTRIBUTE_VALUE_DIST = "workload.attribute_value_dist";
	public static String WLOAD_SUB_OVER_NODES_DIST = "workload.sub_over_nodes_dist";
	public static String WLOAD_ADV_OVER_NODES_DIST = "workload.adv_over_nodes_dist";
	public static String WLOAD_PUB_NODES = "workload.pub_nodes";
	public static String WLOAD_SUB_NODES = "workload.sub_nodes";
	public static String WLOAD_PUB_NODES_LIST = "workload.pub_nodes_list";
	public static String WLOAD_SUB_NODES_LIST = "workload.sub_nodes_list";
	public static String WLOAD_LOAD_FAULTS = "workload.load_faults";
}
