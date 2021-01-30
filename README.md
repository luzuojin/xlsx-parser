# xlsx-parser
lightweight .xlsx file java parser without any dependencies
```java
Workbook workbook = Workbook.getWorkbook(file);
Sheet sheet = workbook.getSheet(index);
for(Row row : sheet) {
  for(Cell cell : row) {
    //do something
  }
}
```
