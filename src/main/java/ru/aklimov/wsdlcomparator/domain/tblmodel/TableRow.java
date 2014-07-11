package ru.aklimov.wsdlcomparator.domain.tblmodel;

/**
 *
 */
public class TableRow {
    static private String CONFLICT_ERR_MSG = "A TableRow can't describe both an element/attribute and an indicator.";
    private ElemAttrDescrRow elemRow;
    private IndicatorDescrRow indicatorDescrRow;
    /**Whether this row describes a reference to a group that presents a root indicator in a complex type*/
    private boolean rootGroupRefRow;
    private boolean empty;

    public TableRow(){
        this.empty = true;
    }

    public TableRow(ElemAttrDescrRow elemAttrDescRow){
        this.elemRow = elemAttrDescRow;
    }

    public TableRow(IndicatorDescrRow indicatorDescrRow){
        this.indicatorDescrRow = indicatorDescrRow;
    }

    public ElemAttrDescrRow getElemRow() {
        return elemRow;
    }

    public void setElemRow(ElemAttrDescrRow elemRow) {
        if(indicatorDescrRow != null){
            throw new RuntimeException(CONFLICT_ERR_MSG);
        }
        this.elemRow = elemRow;
        this.empty = false;
    }

    public IndicatorDescrRow getIndicatorDescrRow() {
        return indicatorDescrRow;
    }

    public void setIndicatorDescrRow(IndicatorDescrRow indicatorDescrRow) {
        if(elemRow != null){
            throw new RuntimeException(CONFLICT_ERR_MSG);
        }
        this.indicatorDescrRow = indicatorDescrRow;
        this.empty = false;
    }

    public boolean isEmpty() {
        return empty;
    }

    public boolean isRootGroupRefRow() {
        return rootGroupRefRow;
    }

    public void setRootGroupRefRow(boolean rootGroupRefRow) {
        this.rootGroupRefRow = rootGroupRefRow;
    }
}
