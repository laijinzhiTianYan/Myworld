<template>
  <div class="app-container">
    <div class="filter-container">
      <el-form>
        <el-form-item>
          <el-button type="primary" @click="showCreate">添加权限</el-button>
        </el-form-item>
      </el-form>
    </div>
    <el-table
      ref="multipleTable"
      :data="list"
      :span-method="objectSpanMethod"
      border
      tooltip-effect="dark"
      style="width: 100%"
      @selection-change="handleSelectionChange">
      <el-table-column align="center" label="序号" width="80">
        <template slot-scope="scope">
          <span v-text="getIndex(scope.$index)"> </span>
        </template>
      </el-table-column>
      <el-table-column prop="menuCode" label="菜单编码" width="200" show-overflow-tooltip></el-table-column>
      <el-table-column prop="menuName" label="菜单名称" width="200"></el-table-column>
      <el-table-column prop="permissionCode" label="权限编码" width="200"></el-table-column>
      <el-table-column prop="permissionName" label="权限名称" width="200"></el-table-column>
      <el-table-column label="管理">
        <template slot-scope="scope">
          <el-button type="primary" @click="showUpdate(scope.$index)">修改</el-button>
          <el-button type="danger"  @click="removeMenu(scope.$index)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-pagination
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
      :current-page="listQuery.pageNum"
      :page-sizes="[20, 30, 40]"
      :page-size='listQuery.pageRow'
      layout="total, sizes, prev, pager, next, jumper"
      :total=this.totalCount>
    </el-pagination>
    <el-dialog :title="textMap[dialogStatus]" :visible.sync="dialogFormVisible">
      <el-form class="small-space" :model="tempMenu" label-position="left" label-width="80px"
               style='width: 300px; margin-left:250px;'>
        <el-form-item label="菜单编码" required>
          <el-input type="text" v-model="tempMenu.menuCode">
          </el-input>
        </el-form-item>
        <el-form-item label="菜单名称" v-if="dialogStatus=='create'" required>
          <el-input type="text" v-model="tempMenu.menuName">
          </el-input>
        </el-form-item>
        <el-form-item label="权限编码" required>
          <el-input type="text" v-model="tempMenu.permissionCode">
          </el-input>
        </el-form-item>
        <el-form-item label="权限名称" required>
          <el-input type="text" v-model="tempMenu.permissionName">
          </el-input>
        </el-form-item>
        <el-form-item>

        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button @click="dialogFormVisible = false">取 消</el-button>
        <el-button v-if="dialogStatus=='create'" type="success" @click="createPermission">创 建</el-button>
        <el-button type="primary" v-else @click="updatePermission">修 改</el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script>
    export default {
      name: "menu1",
      data(){
        return{
          totalCount:0,
          eachCount:[],
          list:[],
          listQuery: {
            pageNum:1,
            pageRow:20
          },
          roles: [],
          multipleSelection:'',
          dialogStatus:'create',
          dialogFormVisible:false,
          textMap: {
            update:'编辑',
            create:'添加权限'
          },
          tempMenu: {
            id:'',
            menuCode:'',
            menuName:'',
            permissionCode:'',
            permissionName:'',
            isRequires:false
          }
        }
      },
      created(){
        this.getList();
      },
      computed:{
        groupNum(){//获得菜单列表数组
          return new Set(this.list.map(o => o.menuCode));
        }
      },
      methods:{
        getList(){
          this.api({
            url:"menu/listMenu",
            method:"get",
            params:this.listQuery
          }).then(data =>{
            this.list = data.list;
            this.totalCount = data.totalCount;
            this.eachCount = data.eachCount;
          })
        },
        handleSizeChange(val) {
          //改变每页数量
          this.listQuery.pageRow = val;
          this.handleFilter();
        },
        handleCurrentChange(val) {
          //改变页码
          this.listQuery.pageNum = val;
          this.getList();
        },
        handleFilter() {
          //查询事件
          this.listQuery.pageNum = 1;
          this.getList()
        },
        getIndex($index) {
          //表格序号
          return (this.listQuery.pageNum - 1) * this.listQuery.pageRow + $index + 1
        },
        showCreate() {
          //显示新增对话框
          this.tempMenu.id = "";
          this.tempMenu.menuCode = "";
          this.tempMenu.menuName = "";
          this.tempMenu.permissionCode = "";
          this.tempMenu.permissionName = "";
          this.dialogStatus = "create";
          this.dialogFormVisible = true
        },
        showUpdate($index) {
          let menu = this.list[$index];
          this.tempMenu.id = menu.id;
          this.tempMenu.menuCode = menu.menuCode;
          this.tempMenu.menuName = menu.menuName;
          this.tempMenu.permissionCode = menu.permissionCode;
          this.tempMenu.permissionName = menu.permissionName;
          this.dialogStatus = "update";
          this.dialogFormVisible = true
        },
        createPermission(){
          //创建新权限
          this.api({
            url: "/menu/addPermission",
            method: "post",
            data: this.tempMenu
          }).then(() => {
            this.getList();
            this.dialogFormVisible = false;
            this.$message({
              type: 'success',
              message: "创建成功!"
            });
          })
        },
        updatePermission(){
          //更新权限
          this.api({
            url: "/menu/updatePermission",
            method: "post",
            data: this.tempMenu
          }).then(() => {
            this.getList();
            this.dialogFormVisible = false;
            this.$message({
              type: 'success',
              message: "更新成功!"
            });
          })
        },
        handleSelectionChange(val) {
          this.multipleSelection = val;
        },
        removeMenu($index){
          let _vue = this;
          this.$confirm('确定删除此Menu?', '提示', {
            confirmButtonText: '确定',
            showCancelButton: false,
            type: 'warning'
          }).then(() => {
            console.log(_vue.list[$index].id);
            let menu = _vue.list[$index];
            menu.deleteStatus = 2;
            _vue.api({
              url: "/menu/deleteMenu",
              method: "post",
              data: menu
            }).then(() => {
              _vue.getList()
            }).catch(() => {
              _vue.$message.error("删除失败")
            })
          })
        },
        menuGroup(menuCode){//根据菜单编码计算该菜单下权限的数量
          return this.list.filter(o => o.menuCode == menuCode).length;
        },
        menuNameLen(menuCode){  //根据菜单编码获取该菜单第一个权限在全量权限中的偏移位置
          const tmp = Array.from(this.groupNum);
          const index = tmp.indexOf(menuCode);  //某菜单在全菜单中的偏移位置
          let len = 0;
          for (let i = 0;i < index;i++){
            len += this.menuGroup(tmp[i]);
          }
          return len;
        },
        objectSpanMethod({ row, column, rowIndex, columnIndex }) {
          /*里面包含当前行row、当前列column、当前行号rowIndex、当前列号columnIndex四个属性*/
          /*相当精彩啊 我竟然想用循环写 学习了 参考：https://www.cnblogs.com/zhang134you/p/11498762.html*/
          if (columnIndex === 1 || columnIndex === 2 || columnIndex === 0){
            const len = this.menuGroup(row.menuCode);
            const lenName = this.menuNameLen(row.menuCode);
            if (rowIndex === lenName) {
              return {
                rowspan:len,
                colspan:1
              }
            } else return {
              rowspan: 0,
              colspan: 0
            };
          } else {
            return {
              rowspan: 1,
              colspan: 1
            };
          }
          /*写死的写法 但是数据库数据变化就不对了*/
           /* if(rowIndex === 0){
              return {
                rowspan: 3,
                colspan: 1
              };
            }else if(rowIndex === 3) {
              return {
                rowspan: 4,
                colspan: 1
              };
            }else if(rowIndex === 7) {
              return {
                rowspan: 4,
                colspan: 1
              };
            }else {
              return {
                rowspan: 0,
                colspan: 0
              };
            }*/
           /*改进的写法，每种菜单下的权限 从后台传过来 我打算用循环 但是不行 附上拙劣的代码*/
          /*let menuLen = this.eachCount.length;
          if (columnIndex === 1 || columnIndex === 2 || columnIndex === 0){
            let rowspan = 0;
            for(let i = 0; i < menuLen;i++){
              if(rowIndex === rowspan) {
                rowspan += this.eachCount[i];
                return {
                  rowspan: this.eachCount[i],
                  colspan: 1
                };
              }else {
                return {
                  rowspan: 0,
                  colspan: 0
                };
              }
            }
          }*/
        }
      }
    }
</script>

<style scoped>
  .filter-container{
    margin-top: 5px;
    margin-left: 5px;
    float: left;
  }
</style>
