<template>
  <div class="test">
    <router-link to="/test">Test</router-link>
    <code-diff :old-string="oldStr" :new-string="newStr" :context="1000" outputFormat="side-by-side" />
    <br/><br/>
    <Table :columns="columns1" :data="data1"></Table>
  </div>
</template>

<script>
import CodeDiff from 'vue-code-diff'
export default {
  name: 'Test',
  components: {CodeDiff},
  props: {
  },
  data(){
    return {
      oldStr: 'old code\nsame code',
      newStr: 'new code\nsame code',
      columns1: [
                    {
                        title: 'Name',
                        key: 'name',
                        sortable: true,
                        // 中文字排序
                        sortMethod(a,b,type){
                          if(type==="asc"){
                            return a.localeCompare(b, 'zh-CN');
                          }else{
                            return b.localeCompare(a, 'zh-CN');
                          }
                        }
                    },
                    {
                        title: 'Age',
                        key: 'age',
                        sortable: true,
                        // 过滤筛选
                        filters: [
                            {
                                label: 'Greater than 25',
                                value: 1
                            },
                            {
                                label: 'Less than 25',
                                value: 2
                            }
                        ],
                        filterMultiple: false,
                        filterMethod (value, row) {
                            if (value === 1) {
                                return row.age > 25;
                            } else if (value === 2) {
                                return row.age < 25;
                            }
                        }
                    },
                    {
                        title: 'Address',
                        key: 'address',
                        sortable: true,
                        // 字母排序
                        sortMethod(a,b,type){
                          if(type==="asc"){
                            return a.localeCompare(b, 'en-US');
                            // return a.charCodeAt(0)-b.charCodeAt(0)
                          }else{
                            return b.localeCompare(a, 'en-US');
                            // return b.charCodeAt(0)-a.charCodeAt(0)
                          }
                        }
                    },
                    {
                      title:"date",
                      key:"date",
                      sortable: true
                    }
                ],
                data1: [
                    {
                        name: '啊',
                        age: 18,
                        address: 'AA',
                        date: '2016-10-03'
                    },
                    {
                        name: '不',
                        age: 24,
                        address: 'AB',
                        date: '2016-10-01'
                    },
                    {
                        name: '才',
                        age: 30,
                        address: 'AC',
                        date: '2016-10-02'
                    },
                    {
                        name: '都',
                        age: 26,
                        address: 'AD',
                        date: '2016-10-04'
                    }
                ]
    }
  },
  methods:{
    sortChange(column,key,order){
      console.log(column,key,order)
    }
  }
}
</script>

<style scoped>
.test{
    font-size: 2em;
}
</style>
