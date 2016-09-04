/*

    This file is part of the iText (R) project.
    Copyright (c) 1998-2016 iText Group NV
    Authors: Bruno Lowagie, Paulo Soares, et al.

    This program is free software; you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License version 3
    as published by the Free Software Foundation with the addition of the
    following permission added to Section 15 as permitted in Section 7(a):
    FOR ANY PART OF THE COVERED WORK IN WHICH THE COPYRIGHT IS OWNED BY
    ITEXT GROUP. ITEXT GROUP DISCLAIMS THE WARRANTY OF NON INFRINGEMENT
    OF THIRD PARTY RIGHTS

    This program is distributed in the hope that it will be useful, but
    WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
    or FITNESS FOR A PARTICULAR PURPOSE.
    See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License
    along with this program; if not, see http://www.gnu.org/licenses or write to
    the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor,
    Boston, MA, 02110-1301 USA, or download the license from the following URL:
    http://itextpdf.com/terms-of-use/

    The interactive user interfaces in modified source and object code versions
    of this program must display Appropriate Legal Notices, as required under
    Section 5 of the GNU Affero General Public License.

    In accordance with Section 7(b) of the GNU Affero General Public License,
    a covered work must retain the producer line in every PDF that is created
    or manipulated using iText.

    You can be released from the requirements of the license by purchasing
    a commercial license. Buying such a license is mandatory as soon as you
    develop commercial activities involving the iText software without
    disclosing the source code of your own applications.
    These activities include: offering paid services to customers as an ASP,
    serving PDFs on the fly in a web application, shipping iText with a closed
    source product.

    For more information, please contact iText Software Corp. at this
    address: sales@itextpdf.com
 */
package com.itextpdf.kernel.pdf;

import com.itextpdf.kernel.PdfException;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.pdf.colorspace.PdfColorSpace;
import com.itextpdf.kernel.pdf.colorspace.PdfPattern;
import com.itextpdf.kernel.pdf.colorspace.PdfShading;
import com.itextpdf.kernel.pdf.extgstate.PdfExtGState;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.kernel.pdf.xobject.PdfImageXObject;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 * Wrapper class that represent resource dictionary - that define named resources
 * used by content streams operators. (ISO 32000-1, 7.8.3 Resource Dictionaries)
 */
public class PdfResources extends PdfObjectWrapper<PdfDictionary> {

    private static final long serialVersionUID = 7160318458835945391L;
	
    private static final String F = "F";
    private static final String Im = "Im";
    private static final String Fm = "Fm";
    private static final String Gs = "Gs";
    private static final String Pr = "Pr";
    private static final String Cs = "Cs";
    private static final String P = "P";
    private static final String Sh = "Sh";

    private Map<PdfObject, PdfName> resourceToName = new HashMap<>();

    private ResourceNameGenerator fontNamesGen = new ResourceNameGenerator(PdfName.Font, F);
    private ResourceNameGenerator imageNamesGen = new ResourceNameGenerator(PdfName.XObject, Im);
    private ResourceNameGenerator formNamesGen = new ResourceNameGenerator(PdfName.XObject, Fm);
    private ResourceNameGenerator egsNamesGen = new ResourceNameGenerator(PdfName.ExtGState, Gs);
    private ResourceNameGenerator propNamesGen = new ResourceNameGenerator(PdfName.Properties, Pr);
    private ResourceNameGenerator csNamesGen = new ResourceNameGenerator(PdfName.ColorSpace, Cs);
    private ResourceNameGenerator patternNamesGen = new ResourceNameGenerator(PdfName.Pattern, P);
    private ResourceNameGenerator shadingNamesGen = new ResourceNameGenerator(PdfName.Shading, Sh);

    private boolean readOnly = false;
    private boolean isModified = false;

    /**
     * Creates new instance from given dictionary.
     * @param pdfObject the {@link PdfDictionary} object from which the resource object will be created.
     */
    public PdfResources(PdfDictionary pdfObject) {
        super(pdfObject);
        buildResources(pdfObject);
    }

    /**
     * Creates new instance from empty dictionary.
     */
    public PdfResources() {
        this(new PdfDictionary());
    }

    /**
     * Adds font to resources and register PdfFont in the document for further flushing.
     *
     * @return added font resource name.
     */
    public PdfName addFont(PdfDocument pdfDocument, PdfFont font) {
        pdfDocument.getDocumentFonts().add(font);
        return addResource(font, fontNamesGen);
    }

    /**
     * Adds {@link PdfImageXObject} object to the resources.
     *
     * @param image the {@link PdfImageXObject} to add.
     * @return added image resource name.
     */
    public PdfName addImage(PdfImageXObject image) {
        return addResource(image, imageNamesGen);
    }

    /**
     * Adds {@link PdfStream} to the resources as image.
     *
     * @param image the {@link PdfStream} to add.
     * @return added image resources name.
     */
    public PdfName addImage(PdfStream image) {
        return addResource(image, imageNamesGen);
    }

    /**
     * Adds {@link PdfObject} to the resources as image.
     *
     * @param image the {@link PdfObject} to add. Should be {@link PdfStream}.
     * @return added image resources name.
     *
     * @deprecated Will be removed in iText 7.1. Use more safe {@link #addImage(PdfStream)} instead.
     */
    @Deprecated
    public PdfName addImage(PdfObject image) {
        if (image.getType() != PdfObject.STREAM) {
            throw new PdfException(PdfException.CannotAddNonStreamImageToResources1)
                    .setMessageParams(image.getClass().toString());
        }
        return addResource(image, imageNamesGen);
    }

    /**
     * Adds {@link PdfFormXObject} object to the resources.
     *
     * @param form the {@link PdfFormXObject} to add.
     * @return added form resource name.
     */
    public PdfName addForm(PdfFormXObject form) {
        return addResource(form, formNamesGen);
    }

    /**
     * Adds {@link PdfStream} to the resources as form.
     *
     * @param form the {@link PdfStream} to add.
     * @return added form resources name.
     */
    public PdfName addForm(PdfStream form) {
        return addResource(form, formNamesGen);
    }

    /**
     * Adds {@link PdfObject} to the resources as form.
     *
     * @param form the {@link PdfObject} to add. Should be {@link PdfStream}.
     * @return added form resources name.
     *
     * @deprecated Will be removed in iText 7.1. Use more safe {@link #addForm(PdfStream)} instead.
     */
    @Deprecated
    public PdfName addForm(PdfObject form) {
        if (form.getType() != PdfObject.STREAM) {
            throw new PdfException(PdfException.CannotAddNonStreamFormToResources1)
                    .setMessageParams(form.getClass().toString());
        }
        return addResource(form, formNamesGen);
    }

    /**
     * Adds the given Form XObject to the current instance of {@link PdfResources}.
     *
     * @param form Form XObject.
     * @param name Preferred name for the given Form XObject.
     * @return the {@link PdfName} of the newly added resource
     */
    public PdfName addForm(PdfFormXObject form, PdfName name) {
        if (getResourceNames(PdfName.XObject).contains(name)) {
            name = addResource(form, formNamesGen);
        } else {
            addResource(form.getPdfObject(), PdfName.XObject, name);
        }

        return name;
    }

    /**
     * Adds {@link PdfExtGState} object to the resources.
     *
     * @param extGState the {@link PdfExtGState} to add.
     * @return added graphics state parameter dictionary resource name.
     */
    public PdfName addExtGState(PdfExtGState extGState) {
        return addResource(extGState, egsNamesGen);
    }

    /**
     * Adds {@link PdfDictionary} to the resources as graphics state parameter dictionary.
     *
     * @param extGState the {@link PdfDictionary} to add.
     * @return added graphics state parameter dictionary resources name.
     */
    public PdfName addExtGState(PdfDictionary extGState) {
        return addResource(extGState, egsNamesGen);
    }

    /**
     * Adds {@link PdfObject} to the resources as graphics state parameter dictionary.
     *
     * @param extGState the {@link PdfObject} to add. Should be {@link PdfDictionary}.
     * @return added graphics state parameter dictionary resources name.
     *
     * @deprecated Will be removed in iText 7.1. Use more safe {@link #addExtGState(PdfDictionary)} instead.
     */
    @Deprecated
    public PdfName addExtGState(PdfObject extGState) {
        if (extGState.getType() != PdfObject.DICTIONARY) {
            throw new PdfException(PdfException.CannotAddNonDictionaryExtGStateToResources1)
                    .setMessageParams(extGState.getClass().toString());
        }
        return addResource(extGState, egsNamesGen);
    }

    /**
     * Adds {@link PdfDictionary} to the resources as properties list.
     *
     * @param properties the {@link PdfDictionary} to add.
     * @return added properties list resources name.
     */
    public PdfName addProperties(PdfDictionary properties) {
        return addResource(properties, propNamesGen);
    }

    /**
     * Adds {@link PdfObject} to the resources as properties list.
     *
     * @param properties the {@link PdfObject} to add. Should be {@link PdfDictionary}.
     * @return added properties list resources name.
     *
     * @deprecated Will be removed in iText 7.1. Use more safe {@link #addProperties(PdfDictionary)} instead.
     */
    @Deprecated
    public PdfName addProperties(PdfObject properties) {
        if (properties.getType() != PdfObject.DICTIONARY) {
            throw new PdfException(PdfException.CannotAddNonDictionaryPropertiesToResources1)
                    .setMessageParams(properties.getClass().toString());
        }
        return addResource(properties, propNamesGen);
    }

    /**
     * Adds {@link PdfColorSpace} object to the resources.
     *
     * @param cs the {@link PdfColorSpace} to add.
     * @return added color space resource name.
     */
    public PdfName addColorSpace(PdfColorSpace cs) {
        return addResource(cs, csNamesGen);
    }

    /**
     * Adds {@link PdfObject} to the resources as color space.
     *
     * @param colorSpace the {@link PdfObject} to add.
     * @return added color space resources name.
     */
    public PdfName addColorSpace(PdfObject colorSpace) {
        return addResource(colorSpace, csNamesGen);
    }

    /**
     * Adds {@link PdfPattern} object to the resources.
     *
     * @param pattern the {@link PdfPattern} to add.
     * @return added pattern resource name.
     */
    public PdfName addPattern(PdfPattern pattern) {
        return addResource(pattern, patternNamesGen);
    }

    /**
     * Adds {@link PdfDictionary} to the resources as pattern.
     *
     * @param pattern the {@link PdfDictionary} to add.
     * @return added pattern resources name.
     */
    public PdfName addPattern(PdfDictionary pattern) {
        return addResource(pattern, patternNamesGen);
    }

    /**
     * Adds {@link PdfObject} to the resources as pattern.
     *
     * @param pattern the {@link PdfObject} to add. Should be {@link PdfDictionary} or {@link PdfStream}.
     * @return added pattern resources name.
     *
     * @deprecated Will be removed in iText 7.1. Use more safe {@link #addPattern(PdfDictionary)} instead.
     */
    @Deprecated
    public PdfName addPattern(PdfObject pattern) {
        if (pattern instanceof PdfDictionary) {
            throw new PdfException(PdfException.CannotAddNonDictionaryPatternToResources1)
                    .setMessageParams(pattern.getClass().toString());
        }
        return addResource(pattern, patternNamesGen);
    }

    /**
     * Adds {@link PdfShading} object to the resources.
     *
     * @param shading the {@link PdfShading} to add.
     * @return added shading resource name.
     */
    public PdfName addShading(PdfShading shading) {
        return addResource(shading, shadingNamesGen);
    }

    /**
     * Adds {@link PdfDictionary} to the resources as shading dictionary.
     *
     * @param shading the {@link PdfDictionary} to add.
     * @return added shading dictionary resources name.
     */
    public PdfName addShading(PdfDictionary shading) {
        return addResource(shading, shadingNamesGen);
    }

    /**
     * Adds {@link PdfObject} to the resources as shading dictionary.
     *
     * @param shading the {@link PdfObject} to add. Should be {@link PdfDictionary} or {@link PdfStream}.
     * @return added shading dictionary resources name.
     *
     * @deprecated Will be removed in iText 7.1. Use more safe {@link #addShading(PdfDictionary)} instead.
     */
    @Deprecated
    public PdfName addShading(PdfObject shading) {
        if (shading instanceof PdfDictionary) {
            throw new PdfException(PdfException.CannotAddNonDictionaryShadingToResources1)
                    .setMessageParams(shading.getClass().toString());
        }
        return addResource(shading, shadingNamesGen);
    }

    protected boolean isReadOnly() {
        return readOnly;
    }

    protected void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    protected boolean isModified() {
        return isModified;
    }

    protected void setModified(boolean isModified) {
        this.isModified = isModified;
    }

    /**
     * Sets the default color space.
     *
     * @param defaultCsKey
     * @param defaultCsValue
     */
    public void setDefaultColorSpace(PdfName defaultCsKey, PdfColorSpace defaultCsValue) {
        addResource(defaultCsValue.getPdfObject(), PdfName.ColorSpace, defaultCsKey);
    }

    public void setDefaultGray(PdfColorSpace defaultCs) {
        setDefaultColorSpace(PdfName.DefaultGray, defaultCs);
    }

    public void setDefaultRgb(PdfColorSpace defaultCs) {
        setDefaultColorSpace(PdfName.DefaultRGB, defaultCs);
    }

    public void setDefaultCmyk(PdfColorSpace defaultCs) {
        setDefaultColorSpace(PdfName.DefaultCMYK, defaultCs);
    }

    public <T extends PdfObject> PdfName getResourceName(PdfObjectWrapper<T> resource) {
        return resourceToName.get(resource.getPdfObject());
    }

    public PdfName getResourceName(PdfObject resource) {
        PdfName resName = resourceToName.get(resource);
        if (resName == null)
            resName = resourceToName.get(resource.getIndirectReference());
        return resName;
    }

    public Set<PdfName> getResourceNames() {
        Set<PdfName> names = new TreeSet<>(); // TODO: isn't it better to use HashSet? Do we really need certain order?
        for (PdfName resType : getPdfObject().keySet()) {
            names.addAll(getResourceNames(resType));
        }
        return names;
    }

    public PdfArray getProcSet() {
        return getPdfObject().getAsArray(PdfName.ProcSet);
    }

    public void setProcSet(PdfArray array) {
        getPdfObject().put(PdfName.ProcSet, array);
    }

    public Set<PdfName> getResourceNames(PdfName resType) {
        PdfDictionary resourceCategory = getPdfObject().getAsDictionary(resType);
        return resourceCategory == null ? new TreeSet<PdfName>() : resourceCategory.keySet(); // TODO: TreeSet or HashSet enough?
    }

    public PdfDictionary getResource(PdfName pdfName) {
        return getPdfObject().getAsDictionary(pdfName);
    }

    @Override
    protected boolean isWrappedObjectMustBeIndirect() {
        return false;
    }

    <T extends PdfObject> PdfName addResource(PdfObjectWrapper<T> resource, ResourceNameGenerator nameGen) {
        return addResource(resource.getPdfObject(), nameGen);
    }

    protected void addResource(PdfObject resource, PdfName resType, PdfName resName) {
        if (resType.equals(PdfName.XObject)) {
            checkAndResolveCircularReferences(resource);
        }
        if (readOnly) {
            setPdfObject(getPdfObject().clone(Collections.<PdfName>emptyList()));
            buildResources(getPdfObject());
            isModified = true;
            readOnly = false;
        }
        if (getPdfObject().containsKey(resType) && getPdfObject().getAsDictionary(resType).containsKey(resName))
            return;
        resourceToName.put(resource, resName);
        PdfDictionary resourceCategory = getPdfObject().getAsDictionary(resType);
        if (resourceCategory == null) {
            getPdfObject().put(resType, resourceCategory = new PdfDictionary());
        }
        resourceCategory.put(resName, resource);
        PdfDictionary resDictionary = (PdfDictionary) getPdfObject().get(resType);
        if (resDictionary == null) {
            getPdfObject().put(resType, resDictionary = new PdfDictionary());
        }
        resDictionary.put(resName, resource);
    }

    PdfName addResource(PdfObject resource, ResourceNameGenerator nameGen) {
        PdfName resName = getResourceName(resource);

        if (resName == null) {
            resName = nameGen.generate(this);
            addResource(resource, nameGen.getResourceType(), resName);
        }

        return resName;
    }

    protected void buildResources(PdfDictionary dictionary) {
        for (PdfName resourceType : dictionary.keySet()) {
            if (getPdfObject().get(resourceType) == null) {
                getPdfObject().put(resourceType, new PdfDictionary());
            }

            PdfDictionary resources = dictionary.getAsDictionary(resourceType);

            if (resources == null) {
                continue;
            }

            for (PdfName resourceName : resources.keySet()) {
                PdfObject resource = resources.get(resourceName, false);
                resourceToName.put(resource, resourceName);
            }
        }
    }

    private void checkAndResolveCircularReferences(PdfObject pdfObject) {
        // Consider the situation when an XObject references the resources of the first page.
        // We add this XObject to the first page, there is no need to resolve any circular references
        // and then we flush this object and try to add it to the second page.
        // Now there are circular references and we cannot resolve them because the object is flushed
        // and we cannot get resources.
        // On the other hand, this situation may occur any time when object is already flushed and we
        // try to add it to resources and it seems difficult to overcome this without keeping /Resources key value.
        if (pdfObject instanceof PdfDictionary && !pdfObject.isFlushed()) {
            PdfDictionary pdfXObject = (PdfDictionary) pdfObject;
            PdfObject pdfXObjectResources = pdfXObject.get(PdfName.Resources);
            if (pdfXObjectResources != null && pdfXObjectResources.getIndirectReference() != null) {
                if (pdfXObjectResources.getIndirectReference().equals(getPdfObject().getIndirectReference())) {
                    PdfObject cloneResources = getPdfObject().clone();
                    cloneResources.makeIndirect(getPdfObject().getIndirectReference().getDocument());
                    pdfXObject.put(PdfName.Resources, cloneResources.getIndirectReference());
                }
            }
        }
    }

    /**
     * Represents a resource name generator. The generator takes into account
     * the names of already existing resources thus providing us a unique name.
     * The name consists of the following parts: prefix (literal) and number.
     */
    static class ResourceNameGenerator implements Serializable {

        private static final long serialVersionUID = 1729961083476558303L;

        private PdfName resourceType;
        private int counter;
        private String prefix;

        /**
         * Constructs an instance of {@link ResourceNameGenerator} class.
         *
         * @param resourceType Type of resource ({@link PdfName#XObject}, {@link PdfName#Font} etc).
         * @param prefix       Prefix used for generating names.
         * @param seed         Seed for the value which is appended to the number each time
         *                     new name is generated.
         */
        public ResourceNameGenerator(PdfName resourceType, String prefix, int seed) {
            this.prefix = prefix;
            this.resourceType = resourceType;
            this.counter = seed;
        }

        /**
         * Constructs an instance of {@link ResourceNameGenerator} class.
         *
         * @param resourceType Type of resource ({@link PdfName#XObject}, {@link PdfName#Font} etc).
         * @param prefix       Prefix used for generating names.
         */
        public ResourceNameGenerator(PdfName resourceType, String prefix) {
            this(resourceType, prefix, 1);
        }

        public PdfName getResourceType() {
            return resourceType;
        }

        /**
         * Generates new (unique) resource name.
         *
         * @return New (unique) resource name.
         */
        public PdfName generate(PdfResources resources) {
            PdfName newName = new PdfName(prefix + counter++);
            PdfDictionary r = resources.getPdfObject();
            if (r.containsKey(resourceType)) {
                while (r.getAsDictionary(resourceType).containsKey(newName)) {
                    newName = new PdfName(prefix + counter++);
                }
            }

            return newName;
        }
    }
}