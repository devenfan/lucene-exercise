# lucene-exercise (lucexer)

这是一个关于Lucene的练习程序，数据从MySQL定时搬运（支持增量和全量同步）。

## 模块说明
| 模块   |  作用   | 依赖 |
| ---    | ---    | --- |
| common | 公共包      | 无  |
| dal    | 数据访问层  | common |
| search | 搜索程序    | common, dal  |
| sync   | 同步程序    | common, dal  |

## 功能说明

分别启动search搜索程序（8090端口）和sync同步程序（8099端口），可以测试如下功能：

1. 查询出 所有 姓"赵"的学生。  
模糊查询  
name:赵*  
http://localhost:8090/search/queryByFamilyName/王

2. 查询出 所有年龄在10-20的 并且从小到大排序。  
range查询（需要预排序支持）  
age:[10 TO 20]  
http://localhost:8090/search/queryByAge?from=10&to=20  

3. 查询出 所有住在"红星区"的，并且考试成绩有一门大于90分的学生列表。  
term查询+range查询  
cityArea: 红星区  
http://localhost:8090/search/queryByCityAreaAndScoreLimit?cityArea=%E7%BA%A2%E6%98%9F%E5%8C%BA&scoreLimit=90  

4. 查询出: 江苏省 ，门牌号是100的，前100个男的。  
http://localhost:8090/search/queryTop100ByProvinceAndSexAndHouseNumber?province=%E6%B1%9F%E8%8B%8F%E7%9C%81&sex=%E7%94%B7&houseNumber=100  

5. 查询出 各姓名的出现的数量 如: 王:100 ,赵:200  
http://localhost:8090/search/summaryByFamilyName

6. 查询出 化学考试不及格的的人各城市的占比。  
http://localhost:8090/search/summaryByCityAndHuaxueFail

7. 查询出: 住别墅的人的成绩是否比不住别墅的人好  
http://localhost:8090/search/summaryScorePerformanceByVillaHouse
