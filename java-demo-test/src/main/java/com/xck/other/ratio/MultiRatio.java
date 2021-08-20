package com.xck.other.ratio;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MultiRatio {

    public static void main(String[] args) {
        List<Ratio> list = generate();
        Map<Integer, List<Ratio>> map = new HashMap<>();
        for(Ratio ratio : list){
            if (ratio.groupMemberRatio > 0) { //按照组比例分组
                List<Ratio> inner = map.get(ratio.groupMemberRatio);
                if(inner == null){
                    map.put(ratio.groupMemberRatio, inner = new ArrayList<>());
                }
                for(int i=0; i<ratio.ratio ; i++){
                    inner.add(ratio);
                }
            }else{
                List<Ratio> inner = map.get(-10);
                if(inner == null){
                    map.put(-10, inner = new ArrayList<>());
                }
                for(int i=0; i<ratio.ratio ; i++){
                    inner.add(ratio);
                }
            }
        }

        List<Integer> groupRatios = new ArrayList<>();
        for(Integer mainRatio : map.keySet()){
            if (mainRatio > 0) { //收集组比例，并按照比例放入list中
                for(int i=0; i<mainRatio; i++){
                    groupRatios.add(mainRatio);
                }
            }
        }
        List<Ratio> noGroupRatios = map.get(-10);
        for(int i=0; i<1500000; i++){
            Ratio ratio = diversion(i, noGroupRatios, map, groupRatios);
            ratio.hitCount++;
        }
        System.out.println(list);
    }

    public static List<Ratio> generate(){
        List<Ratio> list = new ArrayList<>();
        list.add(new Ratio(1,0,1));
        list.add(new Ratio(2,0,1));
        list.add(new Ratio(3,3,1));
        list.add(new Ratio(4,3,2));
        list.add(new Ratio(5,3,3));
        list.add(new Ratio(6,2,1));
        list.add(new Ratio(7,2,2));
        return list;
    }

    public static Ratio diversion(int mobileRandom, List<Ratio> noGroupRatios
            , Map<Integer, List<Ratio>> map, List<Integer> groupRatios){

        //和非组比例一起选择
        Integer mainRatio = mobileRandom%(noGroupRatios.size() + groupRatios.size());
        if(mainRatio > noGroupRatios.size()-1){
            mainRatio -= mainRatio-noGroupRatios.size()-1;
            List<Ratio> groupMemberRatio = map.get(groupRatios.get(mainRatio));
            return groupMemberRatio.get(mobileRandom%groupMemberRatio.size());
        }
        return noGroupRatios.get(mainRatio);
    }

    public static class Ratio{
        public int tag;
        public int groupMemberRatio;
        public int ratio;
        public int hitCount = 0;

        public Ratio(int tag, int groupMemberRatio, int ratio) {
            this.tag = tag;
            this.groupMemberRatio = groupMemberRatio;
            this.ratio = ratio;
        }

        @Override
        public String toString() {
            return "Ratio{" +
                    "tag=" + tag +
                    ", hitCount=" + hitCount +
                    '}';
        }
    }
}
