package com.handpay.ibenefit.framework.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ListUtils {
	private ListUtils() {
	}

	public static List filter(Collection full, Collection has) {
		// List notHas = new ArrayList();
		// for (Object object : full) {
		// if (!has.contains(object)) {
		// notHas.add(object);
		// }
		// }
		// return notHas;
		Set bb = new HashSet(has);
		List ret = new ArrayList();
		for (Object e : full) {
			if (!bb.contains(e)) {
				ret.add(e);
			}
		}
		return ret;
	}

	public static List diff(List ls, List ls2) {
		List list = new ArrayList(Arrays.asList(new Object[ls.size()]));
		Collections.copy(list, ls);
		list.removeAll(ls2);
		return list;
	}

	public static void addAllCollection(Collection c, Collection add) {
		if (add != null) {
			c.addAll(add);
		}
	}

}
