package ru.aklimov.wsdlcomparator.domain.descriptors;

/**
 * This marker interface marks an object as a content item of either some complex type declaration or
 * one from following indicators declaration:<br/>
 * <ul>
 *     <li>sequence</li>
 *     <li>choice</li>
 *     <li>all</li>
 * </ul>
 * <br/>
 * The content may be either an element declaration or an indicator declaration(all, choice e.t.c.)
 *
 */
public interface ParticleContent {
}
