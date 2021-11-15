package com.xck.other;

import java.util.TreeMap;

/**
 * 红黑树
 *
 * @author xuchengkun
 * @date 2021/10/28 09:14
 **/
public class RedBlackTree {

    private TreeNode root;
    private static final boolean BLACK = false;
    private static final boolean RED = true;

    public static void main(String[] args) {
        System.out.println("a" == new String("a"));
    }

    public TreeNode get(int value) {
        TreeNode cur = root;
        while (cur != null) {
            if (cur.value > value) {
                cur = cur.left;
            } else if (cur.value < value) {
                cur = cur.right;
            } else {
                return cur;
            }
        }
        return null;
    }

    public boolean insert(int value) {
        if (root == null) {
            root = new TreeNode(value, BLACK);
            return true;
        }

        TreeNode cur = root, parent = null;
        //寻找位置
        while (cur != null) {
            if (cur.value > value) {
                parent = cur;
                cur = cur.left;
            } else if (cur.value < value) {
                parent = cur;
                cur = cur.right;
            } else {
                return false;
            }
        }

        //判断插入左边还是右边
        cur = new TreeNode(value, RED);
        cur.parent = parent;
        if (parent.value > value) {
            parent.left = cur;
        } else {
            parent.right = cur;
        }

        /**
         * 如果父节点是红色就持续调整，根节点永远是黑色的，所以无需判断
         */
        while (cur != null && colorOf(cur.parent) == RED) {
            TreeNode pp = cur.parent.parent;
            //当前父节点是爷爷的左孩子
            if (cur.parent == pp.left) {
                //父节点是左孩子，那叔叔一定是右孩子
                TreeNode uncle = cur.parent.right;
                if (uncle != null && colorOf(uncle) == RED) { //uncle是红色
                    pp.left.color = BLACK;
                    pp.right.color = BLACK;
                    pp.color = RED;
                    //递推
                    cur = pp;
                } else { //uncle 不存在或者说黑色
                    //如果父节点是爷爷的左孩子，但是当前节点是父节点的右孩子，那么
                    //要左旋，将父子节点转换到同一边
                    //旋转后的，父子节点是颠倒的，cur指向原本父节点的位置
                    if (cur == cur.parent.right) {
                        rotateLeft(cur.parent);
                    }

                    //然后对爷爷节点进行右旋，这样爷爷节点就变成右孩子了，cur节点变成了爷爷节点
                    rotateRight(pp);
                    pp.color = RED;
                    cur.color = BLACK;
                }
            } else { //当前父节点是爷爷的右孩子，倒过来
                TreeNode uncle = cur.parent.left;
                if (uncle != null && colorOf(uncle) == RED) { //uncle是红色
                    pp.left.color = BLACK;
                    pp.right.color = BLACK;
                    pp.color = RED;
                    //递推
                    cur = pp;
                } else { //uncle 不存在或者说黑色
                    //如果父节点是爷爷的右孩子，但是当前节点是父节点的左孩子，那么
                    //要右旋，将父子节点转换到同一边
                    //旋转后的，父子节点是颠倒的，cur指向原本父节点的位置
                    if (cur == cur.parent.left) {
                        rotateRight(cur.parent);
                    }

                    //然后对爷爷节点进行左旋，这样爷爷节点就变成左孩子了，cur节点变成了爷爷节点
                    rotateLeft(pp);
                    pp.color = RED;
                    cur.color = BLACK;
                }
            }
        }
        return false;
    }

    public void delete(int value) {
        TreeNode cur = root;
        //寻找位置
        while (cur != null) {
            if (cur.value > value) {
                cur = cur.left;
            } else if (cur.value < value) {
                cur = cur.right;
            } else {
                break;
            }
        }

        if (cur.left != null && cur.right != null) {
            cur = successor(cur); //寻找后继节点
        }

        TreeNode replace = (cur.left != null ? cur.left : cur.right);
        //1. 说明有孩子节点
        if (replace != null) {
            //给孩子节点确认父节点
            replace.parent = cur.parent;
            if (cur.parent == null) {
                root = replace;
            } else if (cur.parent.left == cur) { //(1) 删除节点是左孩子
                cur.parent.left = replace;
            } else { //(2) 删除节点是右孩子
                cur.parent.right = replace;
            }

            cur.left = cur.right = cur.parent = null;
            if (colorOf(cur) == BLACK) {
                fixAfterDeletion(replace); //调整replace
            }
        } else if (cur.parent == null) { //2. 只有一个节点
            root = null;
        } else { //3. 没有孩子节点，删除的是叶子节点
            if (colorOf(cur) == BLACK) {
                fixAfterDeletion(cur);
            }

            /**
             * cur和父节点断链
             */
            if (cur.parent != null) {
                if (cur == cur.parent.left)
                    cur.parent.left = null;
                else if (cur == cur.parent.right)
                    cur.parent.right = null;
                cur.parent = null;
            }
        }
    }

    /**
     * 寻找node节点的后继节点
     *
     * @param node
     * @return
     */
    public TreeNode successor(TreeNode node) {
        if (node == null) return null;

        //如果有右子树，那么一直往右子树的左边找即可
        if (node.right != null) {
            TreeNode p = node.right;
            while (p.left != null) {
                p = p.left;
            }
            return p;
        }

        TreeNode p = node.parent;
        TreeNode cur = node;
        //cur节点是父节点的右孩子
        while (p != null && cur == p.right) {
            cur = p;
            p = p.parent;
        }
        return p;
    }

    private void fixAfterDeletion(TreeNode node) {
        /**
         * 红色节点不影响删除，无需调整
         */
        while (node != root && colorOf(node) == BLACK) {
            //1. 待删除节点是左孩子
            if (node == node.parent.left) {
                TreeNode uncle = node.parent.right;

                //2. 兄弟节点是红色：父节点(黑色)和兄弟节点颜色互换，然后左旋
                if (colorOf(uncle) == RED) {
                    uncle.color = BLACK;
                    node.parent.color = RED;
                    rotateLeft(node.parent);
                    uncle = node.parent.right; //此时兄弟节点是黑色
                }

                //3-1. 兄弟节点的孩子节点都是黑色：那只要兄弟节点置为红色就行了
                if (colorOf(uncle.left) == BLACK &&
                        colorOf(uncle.right) == BLACK) {
                    uncle.color = RED;
                    node = node.parent; //往上
                } else { //3-2. 兄弟节点的孩子节点不全是黑色
                    //3-2-1：如果右孩子是黑色，说明左边是红色，需要对uncle进行右旋
                    if (colorOf(uncle.right) == BLACK) {
                        //对调颜色，兄弟节点设置为红色，这样右旋后就是右孩子是节点
                        uncle.color = RED;
                        uncle.left.color = BLACK;
                        rotateRight(uncle);
                        uncle = node.parent.right; //重新获取兄弟节点
                    }

                    //3-2-2：此时兄弟节点的右孩子是红色，为了补足左侧缺少的黑色，
                    //我们将：父节点置为黑；兄弟节点置为父节点颜色；兄弟节点的右孩子置为黑色
                    //对父节点进行左旋
                    //这样左右黑色数量不变
                    uncle.color = colorOf(node.parent);
                    node.parent.color = BLACK;
                    uncle.right.color = RED;
                    rotateLeft(node.parent);
                    node = root;
                }

            } else { //对称
                TreeNode uncle = node.parent.left;

                //2. 兄弟节点是红色：父节点(黑色)和兄弟节点颜色互换，然后右旋
                if (colorOf(uncle) == RED) {
                    uncle.color = BLACK;
                    node.parent.color = RED;
                    rotateRight(node.parent);
                    uncle = node.parent.left; //此时兄弟节点是黑色
                }

                //3-1. 兄弟节点的孩子节点都是黑色：那只要兄弟节点置为红色就行了
                if (colorOf(uncle.left) == BLACK &&
                        colorOf(uncle.right) == BLACK) {
                    uncle.color = RED;
                    node = node.parent; //往上
                } else { //3-2. 兄弟节点的孩子节点不全是黑色
                    //3-2-1：如果右孩子是黑色，说明左边是红色，需要对uncle进行右旋
                    if (colorOf(uncle.left) == BLACK) {
                        //对调颜色，兄弟节点设置为红色，这样右旋后就是右孩子是节点
                        uncle.color = RED;
                        uncle.left.color = BLACK;
                        rotateLeft(uncle);
                        uncle = node.parent.left; //重新获取兄弟节点
                    }

                    //3-2-2：此时兄弟节点的右孩子是红色，为了补足左侧缺少的黑色，
                    //我们将：父节点置为黑；兄弟节点置为父节点颜色；兄弟节点的右孩子置为黑色
                    //对父节点进行左旋
                    //这样左右黑色数量不变
                    uncle.color = colorOf(node.parent);
                    node.parent.color = BLACK;
                    uncle.right.color = RED;
                    rotateRight(node.parent);
                    node = root;
                }
            }
        }
    }

    private void rotateLeft(TreeNode p) {
        if (p == null) return;

        //1. 记住右孩子节点，右孩子是要成为父节点的
        TreeNode r = p.right;
        //2. 左旋后，右孩子成为父节点，其左子树需要地方放
        p.right = r.left;
        if (r.left != null) {
            r.left.parent = p; //给左子树确认父节点
        }

        //3. 给右孩子确认父节点
        r.parent = p.parent;
        if (p.parent == null) {
            root = r;
        } else if (p.parent.left == p) { //给爷爷节点确定孩子
            p.parent.left = r;
        } else {
            p.parent.right = r;
        }

        r.left = p;
        p.parent = r;
    }

    private void rotateRight(TreeNode p) {
        if (p == null) return;

        TreeNode r = p.left;
        p.left = r.right;
        if (r.right != null) {
            r.right.parent = p;
        }

        r.parent = p.parent;
        if (p.parent == null) {
            root = r;
        } else if (p.parent.left == p) {
            p.parent.left = r;
        } else {
            p.parent.right = r;
        }

        r.right = p;
        p.parent = r;
    }

    public boolean colorOf(TreeNode node) {
        return node == null ? BLACK : RED;
    }

    public class TreeNode {
        TreeNode parent;
        TreeNode right;
        TreeNode left;
        int value;
        boolean color; //默认黑色

        public TreeNode(int value, boolean color) {
            this.value = value;
            this.color = color;
        }
    }
}
