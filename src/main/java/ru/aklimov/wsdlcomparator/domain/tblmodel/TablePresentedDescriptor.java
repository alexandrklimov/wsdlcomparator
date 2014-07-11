package ru.aklimov.wsdlcomparator.domain.tblmodel;

/**
 * This interface marks classes that may be presented as standalone table by some view.<br/>
 * For example {@link ru.aklimov.wsdlcomparator.domain.tblmodel.WSMethodDescrTable} class
 * can't be presented as a standalone table, because complete information about any WS method can consist of two tables:
 * <ul>
 *     <li>request type table</li>
 *     <li>response type table <strong>(optional)</strong></li>
 * </ul>
 * This interface support some code structure development convenience.
 */
public interface TablePresentedDescriptor {
    String getId();
    TableRow getRootRow();
}
