package cn.itcast.job.task.checkversion;

import cn.itcast.job.cache.ConfigConstant;
import cn.itcast.job.pojo.dependices.DependicesNode;
import cn.itcast.job.utils.FileUtil;
import cn.itcast.job.utils.StringUtil;

import java.io.IOException;
import java.util.Stack;

public class DependicesFileReadAndGetTree {
    public static DependicesNode root;
    public static Stack<DependicesNode> stack = new Stack<>();

    public static DependicesNode dependicesFileReadAndGetTree() {

        FileUtil.readFileEveryLine(ConfigConstant.DEPENDICES_PATH,
                new FileUtil.ControlFileEveryLineCallback() {
                    @Override
                    public void control(String line) throws IOException {
                        if (line != null && line.contains("--- ") && StringUtil.search(line, ":") == 2) {
                            if (root == null) {
                                root = new DependicesNode(true, null);
                                stack.push(root);
                            }

                            DependicesNode node = new DependicesNode(line);
                            System.out.println("node " + node.toString());
                            if (stack.peek().numOfShugang < node.numOfShugang) {
//                                node.parent = stack.peek();
                                node.setParent(stack.peek());
                                stack.push(node);

                            } else if (stack.peek().numOfShugang == node.numOfShugang) {
                                stack.pop();
//                                node.parent = stack.peek();
                                node.setParent(stack.peek());
                                stack.push(node);
                            } else if (stack.peek().numOfShugang > node.numOfShugang) {
                                while(!stack.isEmpty() && stack.peek().numOfShugang>=node.numOfShugang){
                                    stack.pop();
                                }
                                DependicesNode parent = stack.peek();
                                stack.push(node);
//                                node.parent = parent;
                                node.setParent(parent);
                            }
                        }
                    }
                });
        System.out.println("root " + root);
        return root;
    }
}
