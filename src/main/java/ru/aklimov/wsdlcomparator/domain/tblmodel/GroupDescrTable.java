package ru.aklimov.wsdlcomparator.domain.tblmodel;

/**
 * Created by aklimov on 04.03.14.
 */
public class GroupDescrTable implements TablePresentedDescriptor {
    private String id;
    private String title;
    private String name;
    private String namespace;
    private boolean isNew;
    private boolean changed;
    private boolean removed;

    //describes all table content from one start point
    private TableRow rootRow = new TableRow();

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNamespace() {
        return namespace;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public boolean isNew() {
        return isNew;
    }

    public void setNew(boolean isNew) {
        this.isNew = isNew;
    }

    public boolean isChanged() {
        return changed;
    }

    public void setChanged(boolean changed) {
        this.changed = changed;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    @Override
    public TableRow getRootRow() {
        return rootRow;
    }

    public void setRootRow(TableRow rootRow) {
        this.rootRow = rootRow;
    }
}
