package ru.aklimov.wsdlcomparator.domain.diff;

import ru.aklimov.wsdlcomparator.domain.descriptors.ParticleContent;

import java.util.Map;

/**
 * {@inheritDoc}
 * <br/>
 * This interface describes types may contain some information about changes of child items.
 *
 */
public interface IDiffInfoWithAffected extends IDiffInfo{

    Map<ParticleContent, ChangeInfoDetails> getAffectedItems();

}
