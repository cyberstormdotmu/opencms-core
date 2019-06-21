/*
 * This library is part of OpenCms -
 * the Open Source Content Management System
 *
 * Copyright (c) Alkacon Software GmbH & Co. KG (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package org.opencms.ade.detailpage;

import org.opencms.ade.configuration.CmsADEManager;
import org.opencms.configuration.CmsParameterConfiguration;
import org.opencms.file.CmsObject;
import org.opencms.file.CmsResource;
import org.opencms.main.CmsException;
import org.opencms.main.OpenCms;
import org.opencms.site.CmsSite;
import org.opencms.workplace.CmsWorkplace;

import java.util.ArrayList;
import java.util.Collection;

/**
 * This class uses information from the detail page information stored in the sitemap to find the detail page for
 * a given resource.<p>
 *
 * @since 8.0.0
 */
public class CmsSitemapDetailPageHandler implements I_CmsDetailPageHandler {

    /** The configuration. */
    private CmsParameterConfiguration m_config = new CmsParameterConfiguration();

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#addConfigurationParameter(java.lang.String, java.lang.String)
     */
    public void addConfigurationParameter(String paramName, String paramValue) {

        m_config.add(paramName, paramValue);

    }

    /**
     * @see org.opencms.ade.detailpage.I_CmsDetailPageHandler#getAllDetailPages(org.opencms.file.CmsObject, int)
     */
    public Collection<String> getAllDetailPages(CmsObject cms, int resType) throws CmsException {

        if (!OpenCms.getADEManager().isInitialized()) {
            return new ArrayList<String>();
        }
        String typeName = OpenCms.getResourceManager().getResourceType(resType).getTypeName();
        return OpenCms.getADEManager().getDetailPages(cms, typeName);
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#getConfiguration()
     */
    public CmsParameterConfiguration getConfiguration() {

        return m_config;

    }

    /**
     * @see org.opencms.ade.detailpage.I_CmsDetailPageHandler#getDetailPage(org.opencms.file.CmsObject, java.lang.String, java.lang.String, java.lang.String)
     */
    public String getDetailPage(CmsObject cms, String rootPath, String linkSource, String targetDetailPage) {

        CmsADEManager manager = OpenCms.getADEManager();
        if (!manager.isInitialized()) {
            return null;
        }

        if (rootPath.endsWith(".jsp") || rootPath.startsWith(CmsWorkplace.VFS_PATH_WORKPLACE)) {
            // exclude these for performance reasons
            return null;
        }
        String result = CmsADEManager.getDetailPage(manager, cms, rootPath, linkSource, targetDetailPage);
        if (result == null) {
            return null;
        }
        if (!CmsResource.isFolder(result)) {
            result = CmsResource.getFolderPath(result);
        }
        return result;
    }

    /**
     * @see org.opencms.configuration.I_CmsConfigurationParameterHandler#initConfiguration()
     */
    public void initConfiguration() {

        m_config = CmsParameterConfiguration.unmodifiableVersion(m_config);

    }

    /**
     * @see org.opencms.ade.detailpage.I_CmsDetailPageHandler#initialize(org.opencms.file.CmsObject, org.opencms.file.CmsObject)
     */
    public void initialize(CmsObject offlineCms, CmsObject onlineCms) {

        // do nothing
    }

    /**
     * Checks whether the given detail page is valid for the given resource.<p>
     *
     * @param cms the CMS context
     * @param page the detail page
     * @param detailRes the detail resource
     *
     * @return true if the given detail page is valid
     */
    public boolean isValidDetailPage(CmsObject cms, CmsResource page, CmsResource detailRes) {

        if (OpenCms.getSystemInfo().isRestrictDetailContents()) {
            // in 'restrict detail contents mode', do not allow detail contents from a real site on a detail page of a different real site
            CmsSite pageSite = OpenCms.getSiteManager().getSiteForRootPath(page.getRootPath());
            CmsSite detailSite = OpenCms.getSiteManager().getSiteForRootPath(detailRes.getRootPath());
            if ((pageSite != null)
                && (detailSite != null)
                && !pageSite.getSiteRoot().equals(detailSite.getSiteRoot())) {
                return false;
            }
        }
        return OpenCms.getADEManager().isDetailPage(cms, page);
    }

}
