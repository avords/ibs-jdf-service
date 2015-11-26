package com.handpay.ibenefit.framework.util;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import com.handpay.ibenefit.framework.entity.BaseTree;
/**
 * Tree Utility
 * @author pubx
 *
 */
public final class TreeUtils {
	private TreeUtils(){
	}

	public static DefaultMutableTreeNode getTree(long parentId, List<? extends BaseTree> allBaseTrees) {
		BaseTree parent = null;
		if(null!=allBaseTrees){
			for (int i=0,n=allBaseTrees.size();i<n;i++) {
				BaseTree baseTree = allBaseTrees.get(i);
				if (parentId == baseTree.getObjectId()) {
					parent = baseTree;
					allBaseTrees.remove(i);
					break;
				}
			}
		}
		DefaultMutableTreeNode treeNode = getTree(parent, allBaseTrees);
		treeNode = buildNotEmptyTree(treeNode);
		return treeNode;
	}
	public static DefaultMutableTreeNode getTree(List<? extends BaseTree> allChannels) {
		DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode("ROOT");
		if(null!=allChannels){
			for (int i=0,n=allChannels.size();i<n;i++) {
				BaseTree baseTree = allChannels.get(i);
				if (baseTree.getParentId() == null) {
					DefaultMutableTreeNode firstNode = new DefaultMutableTreeNode(baseTree);
					List<BaseTree> trees = getTree(baseTree,firstNode,allChannels);
					if (trees.size() > 0) {
						for (BaseTree baseTree1 : trees) {
							firstNode.add(getTree(baseTree1, allChannels));
						}
					}
					treeNode.add(firstNode);
				}
			}
		}
		treeNode = buildNotEmptyTree(treeNode);
		return treeNode;
    }
	
	private static List<BaseTree> getTree(BaseTree parentMenu,DefaultMutableTreeNode firstNode, List<? extends BaseTree> allBaseTrees) {
			List<BaseTree> trees = new ArrayList<BaseTree>();
			for (BaseTree baseTree : allBaseTrees) {
				if (parentMenu.getObjectId().equals(baseTree.getParentId())) {
					trees.add(baseTree);
				}
			}
			return trees;
	}
	private static DefaultMutableTreeNode getTree(BaseTree parentMenu, List<? extends BaseTree> allBaseTrees) {
		if (parentMenu != null) {
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(parentMenu);
			List<BaseTree> trees = new ArrayList<BaseTree>();
			for (BaseTree baseTree : allBaseTrees) {
				if (parentMenu.getObjectId().equals(baseTree.getParentId())) {
					trees.add(baseTree);
				}
			}
			if (trees.size() > 0) {
				for (BaseTree baseTree : trees) {
					treeNode.add(getTree(baseTree, allBaseTrees));
				}
			}
			return treeNode;
		}
		return null;
	}

	public static DefaultMutableTreeNode buildNotEmptyTree(DefaultMutableTreeNode node){
		List<DefaultMutableTreeNode> needDeleted = new ArrayList<DefaultMutableTreeNode>();
		if(null!=node){
			for (Enumeration<DefaultMutableTreeNode> e = node.children(); e.hasMoreElements();) {
				DefaultMutableTreeNode current = e.nextElement();
				DefaultMutableTreeNode childMenu = buildNotEmptyTree(current);
				if(null==childMenu){
					needDeleted.add(current);
				}
			}
			for(DefaultMutableTreeNode current : needDeleted){
				node.remove(current);
			}
		}
		return node;
	}
}
