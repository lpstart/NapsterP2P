package napster.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 *
 * @param <T>
 *            server which can provide index service
 */
public class HashUtil<T> {
	// the relation between virtual node and physical
	private TreeMap<Long, T> virtualNodes;
	// physical node
	private List<T> physicalNodes;
	// number of virtual node per physical node
	private final int NODE_NUM = 100;
	private final int REPCLIC_NUM = 3;

	public HashUtil(List<T> shards) {
		this.physicalNodes = shards;
		initialize();
	}

	/**
	 * initialize a node loop
	 */
	private void initialize() {
		virtualNodes = new TreeMap<Long, T>();
		for (int i = 0; i != physicalNodes.size(); ++i) {
			final T shardInfo = physicalNodes.get(i);

			for (int n = 0; n < NODE_NUM; n++)
				// create NODE_NUM virtual node for each physical node
				virtualNodes.put(hash("virtualNode-" + i + "-node-" + n), shardInfo);
		}
	}

	/**
	 * get the physical node corresponding with key
	 * 
	 * @param key
	 * @return
	 */
	public List<T> getShardInfo(String key) {
		// find the virtual nodes hash value of which are not less than key's
		SortedMap<Long, T> tail = virtualNodes.tailMap(hash(key));
		if (tail.size() == 0) {
			// if no, return the physical node
			return getNObjectFromMap(virtualNodes, REPCLIC_NUM);
		}
		return getNObjectFromMap(tail, REPCLIC_NUM); // return the physical node
	}

	/**
	 * get N physical node from node circle
	 * 
	 * @param map
	 * @param num
	 * @return
	 */
	public List<T> getNObjectFromMap(SortedMap<Long, T> map, int num) {
		Set<Long> keySet = map.keySet();
		ArrayList<T> result = new ArrayList<>();
		int count = 0;
		for (Long temp : keySet) {
			if (count++ < num) {
				result.add(map.get(temp));
			} else {
				break;
			}
		}

		if (result.size() < num) {
			keySet = virtualNodes.keySet();
			for (Long temp : keySet) {
				if (count++ < num) {
					result.add(virtualNodes.get(temp));
				} else {
					break;
				}
			}
		}
		return result;
	}

	/**
	 * get the hash value of the key <br/>
	 * see http://murmurhash.googlepages.com/
	 * 
	 * @param key
	 * @return
	 */
	private Long hash(String key) {
		ByteBuffer buf = ByteBuffer.wrap(key.getBytes());
		int seed = 0x1234ABCD;
		ByteOrder byteOrder = buf.order();
		buf.order(ByteOrder.LITTLE_ENDIAN);
		long m = 0xc6a4a7935bd1e995L;
		int r = 47;

		long h = seed ^ (buf.remaining() * m);

		long k;
		while (buf.remaining() >= 8) {
			k = buf.getLong();

			k *= m;
			k ^= k >>> r;
			k *= m;

			h ^= k;
			h *= m;
		}

		if (buf.remaining() > 0) {
			ByteBuffer finish = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
			finish.put(buf).rewind();
			h ^= finish.getLong();
			h *= m;
		}

		h ^= h >>> r;
		h *= m;
		h ^= h >>> r;

		buf.order(byteOrder);
		return h;
	}

}
