<template>
  <div class="card-rail space-y-4">
    <div>
      <div class="flex items-center justify-between mb-2">
        <div class="font-medium">选课统计</div>
        <div class="text-sm text-gray-500">概览</div>
      </div>
      <div class="grid grid-cols-2 gap-2">
        <div class="bg-white rounded p-3 shadow-sm">
          <div class="text-sm text-gray-500">总选课数</div>
          <div class="text-xl font-bold">1,234</div>
        </div>
        <div class="bg-white rounded p-3 shadow-sm">
          <div class="text-sm text-gray-500">活跃学生</div>
          <div class="text-xl font-bold">890</div>
        </div>
      </div>
    </div>

    <div>
      <div class="font-medium mb-2">选课趋势</div>
      <div ref="miniChart" style="height:140px;"></div>
    </div>

    <div>
      <div class="font-medium mb-2">课程热度</div>
      <div ref="barChart" style="height:120px;"></div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
export default {
  name: 'SideRail',
  mounted() {
    // mini line
    const mini = echarts.init(this.$refs.miniChart)
    mini.setOption({
      tooltip: { show: false },
      grid: { left: 0, right: 0, top: 10, bottom: 10 },
      xAxis: { type: 'category', data: ['周一','周二','周三','周四','周五'], show:false },
      yAxis: { type: 'value', show:false },
      series: [{ type: 'line', smooth: true, data: [120,200,150,80,70], areaStyle: {color: 'rgba(99,102,241,0.12)'}, lineStyle:{color:'#6366f1'} }]
    })

    const bar = echarts.init(this.$refs.barChart)
    bar.setOption({
      tooltip: {},
      grid:{left:0,right:0,top:10,bottom:10},
      xAxis:{type:'category', data:['Math','CS','Eng','Phy'], axisLabel:{show:false}},
      yAxis:{type:'value', show:false},
      series:[{type:'bar', data:[60,95,40,30], itemStyle:{color:'#10b981'}}]
    })

    window.addEventListener('resize', () => { mini.resize(); bar.resize(); })
  }
}
</script>
