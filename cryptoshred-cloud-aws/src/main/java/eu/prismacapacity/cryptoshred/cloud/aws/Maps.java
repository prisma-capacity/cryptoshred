package eu.prismacapacity.cryptoshred.cloud.aws;

import java.util.HashMap;

public class Maps {

	public static <K, V> HashMap<K, V> of(K k, V v) {
		HashMap<K, V> ret = new HashMap<K, V>();
		ret.put(k, v);
		return ret;
	}

}
