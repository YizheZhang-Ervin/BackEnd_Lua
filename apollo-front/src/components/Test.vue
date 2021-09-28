<template>
	<div class="test">
		<router-link to="/test">Test</router-link>
		<code-diff
			:old-string="oldStr"
			:new-string="newStr"
			:context="1000"
			outputFormat="side-by-side"
		/>
		<br /><br />
		<Input
			search
			enter-button
			placeholder="任意字段搜索"
			@on-search="searchByKey"
			v-model="searchKey"
		/>
		<Button @click="resetSearchByKey">重置</Button>
		<Table
			:columns="columns2"
			:data="display_data1"
			@on-sort-change="changeSort"
			@on-filter-change="changeFilter"
		></Table>
		<Page
			:total="pageInfo.dataTotal"
			:current="pageInfo.currentPage"
			:page-size="pageInfo.pageSize"
			:page-size-opts="pageInfo.pageSizeOpts"
			show-sizer
			show-total
			@on-change="changePage"
			@on-page-size-change="changePageSize"
		/>
	</div>
</template>

<script>
import CodeDiff from "vue-code-diff";
export default {
	name: "Test",
	components: { CodeDiff },
	props: {},
	data() {
		return {
			useFilter: false,
			lastFilterCol: "",
			searchKey: "",
			oldStr: "old code\nsame code",
			newStr: "new code\nsame code",
			pageInfo: {
				dataTotal: 1,
				currentPage: 1,
				pageSize: 2,
				pageSizeOpts: [2, 5, 10]
			},
			columns1: [
				{
					title: "Name",
					key: "name",
					sortable: true,
					// 中文字排序
					sortMethod(a, b, type) {
						if (type === "asc") {
							return a.localeCompare(b, "zh-CN");
						} else {
							return b.localeCompare(a, "zh-CN");
						}
					},
				},
				{
					title: "Age",
					key: "age",
					sortable: true,
					// 过滤筛选
					filters: [
						{
							label: "Greater than 25",
							value: 1,
						},
						{
							label: "Less than 25",
							value: 2,
						},
					],
					filterMultiple: false,
					filterMethod(value, row) {
						if (value === 1) {
							return row.age > 25;
						} else if (value === 2) {
							return row.age < 25;
						}
					},
				},
				{
					title: "Address",
					key: "address",
					sortable: true,
					// 字母排序
					sortMethod(a, b, type) {
						if (type === "asc") {
							return a.localeCompare(b, "en-US");
							// return a.charCodeAt(0)-b.charCodeAt(0)
						} else {
							return b.localeCompare(a, "en-US");
							// return b.charCodeAt(0)-a.charCodeAt(0)
						}
					},
				},
				{
					title: "date",
					key: "date",
					sortable: true,
				},
			],
			columns2: [
				{
					title: "Name",
					key: "name",
					sortable: 'custom'
				},
				{
					title: "Age",
					key: "age",
					sortable: true,
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
					filterRemote(value, field) {
						console.log(value, field)
					}
					// filterMethod(value, row) {
					// 	if (value === 1) {
					// 		return row.age > 25;
					// 	} else if (value === 2) {
					// 		return row.age < 25;
					// 	}
					// }
				},
				{
					title: "Address",
					key: "address",
					sortable: true,
				},
				{
					title: "date",
					key: "date",
					sortable: true,
				},
			],
			data1: [
				{
					name: "啊",
					age: 18,
					address: "AA",
					date: "2016-10-03",
				},
				{
					name: "不",
					age: 24,
					address: "AB",
					date: "2016-10-01",
				},
				{
					name: "才",
					age: 30,
					address: "AC",
					date: "2016-10-02",
				},
				{
					name: "都",
					age: 26,
					address: "AD",
					date: "2016-10-04",
				},
			],
			data1_raw: [],
			display_data1: [],
			filter_data1: []
		};
	},
	mounted() {
		this.getData()
	},
	methods: {
		// 所有字段中查询
		searchByKey(value) {
			// console.log(value)
			// 关闭过滤器
			this.closeFilter()
			// 判断查询值是否空
			if (value === undefined || value === "") {
				this.data1 = this.data1_raw
				this.changePage(1)
				return
			}
			let temp = this.data1_raw.filter(data => data.name.indexOf(value) !== -1
				|| data.address.indexOf(value) !== -1
				|| data.age.toString().indexOf(value) !== -1
				|| data.date.indexOf(value) !== -1)
			// 判断是否使用过滤器
			this.useFilter ? this.filter_data1 = temp : this.data1 = temp
			this.changePage(1)
		},
		// 重置
		resetSearchByKey() {
			this.searchKey = ""
			// 关闭过滤器
			this.closeFilter()
			// 重置源数据
			this.data1 = this.data1_raw
			this.getData()
		},
		// 获取数据
		getData() {
			// 原始数据
			this.data1_raw = this.data1
			// 给表格设置数据
			this.changePage(1)
		},
		// 换页
		changePage(page = 1) {
			// console.log("当前页", page)
			this.pageInfo.currentPage = page
			let start = (this.pageInfo.currentPage - 1) * this.pageInfo.pageSize
			let end = this.pageInfo.currentPage * this.pageInfo.pageSize
			// 判断是否使用过滤器
			let usedData = this.useFilter ? this.filter_data1 : this.data1
			// 设置总数据条数
			this.pageInfo.dataTotal = usedData.length
			this.display_data1 = usedData.slice(start, end)
		},
		// 换每页显示条数
		changePageSize(pageSize) {
			// console.log("每页显示条数", pageSize)
			this.pageInfo.pageSize = pageSize
			this.changePage(1)
		},
		// 排序改变
		changeSort({ key, order }) {
			// console.log(key, order)
			// 排序方法
			let sortMethod = (a, b) => {
				return a[key].toString().localeCompare(b[key], "zh-CN");
			}
			// 判断是否使用过滤器
			let usedData = this.useFilter ? this.filter_data1 : this.data1
			// 进行排序
			let rst = []
			rst = usedData.sort(sortMethod)
			// 判断升序降序
			this.display_data1 = (order === "asc") ? rst : rst.reverse()
			// 分页
			this.changePage(1)
		},
		// 筛选改变
		changeFilter(col) {
			// console.log(col)
			this.lastFilterCol = col
			let filter = col._filterChecked
			let field = col.key
			// 使用过滤器
			this.useFilter = true
			// 选出大于25的
			if (filter == 1) {
				this.filter_data1 = this.data1.filter(data => data[field] >= 25)
				// 选出小于25的
			} else if (filter == 2) {
				this.filter_data1 = this.data1.filter(data => data[field] < 25)
				// 全部数据
			} else {
				this.closeFilter()
				this.filter_data1 = this.data1
			}
			// 分页
			this.changePage(1)
		},
		// 关闭过滤器
		closeFilter() {
			this.useFilter = false
			this.lastFilterCol._isFiltered = false
			this.lastFilterCol._filterChecked = []
		}
	},
};
</script>

<style scoped>
.test {
	font-size: 2em;
}
</style>
