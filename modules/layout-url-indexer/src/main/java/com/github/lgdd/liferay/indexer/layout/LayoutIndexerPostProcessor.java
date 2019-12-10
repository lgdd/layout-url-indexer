package com.github.lgdd.liferay.indexer.layout;

import com.liferay.portal.kernel.log.Log;
import com.liferay.portal.kernel.log.LogFactoryUtil;
import com.liferay.portal.kernel.model.Company;
import com.liferay.portal.kernel.model.Group;
import com.liferay.portal.kernel.model.Layout;
import com.liferay.portal.kernel.search.*;
import com.liferay.portal.kernel.search.filter.BooleanFilter;
import com.liferay.portal.kernel.service.CompanyLocalService;
import com.liferay.portal.kernel.service.GroupLocalService;
import com.liferay.portal.kernel.service.LayoutLocalService;
import com.liferay.portal.kernel.util.GetterUtil;
import com.liferay.portal.kernel.util.PortalUtil;
import com.liferay.portal.kernel.util.PropsUtil;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Locale;

@Component(
	immediate = true,
	property = {
		"indexer.class.name=com.liferay.portal.kernel.model.Layout",
	},
	service = IndexerPostProcessor.class
)
public class LayoutIndexerPostProcessor implements IndexerPostProcessor {

	@Override
	public void postProcessContextBooleanFilter(
									BooleanFilter booleanFilter, SearchContext searchContext)
									throws Exception {
	}

	@Override
	public void postProcessDocument(Document document, Object obj)
									throws Exception {

		Layout layout = (Layout) obj;
		if (!layout.isSystem() && !layout.isHidden()) {
			Group group = _groupLocalService.getGroup(layout.getGroupId());
			Company company = _companyLocalService.getCompany(group.getCompanyId());
			String portalURL = company.getPortalURL(layout.getGroupId());
			String context = layout.isPrivateLayout() ? PortalUtil.getPathFriendlyURLPrivateGroup() :
											PortalUtil.getPathFriendlyURLPublic();
			String virtualHostSiteName =
											"guest".equalsIgnoreCase(GetterUtil.getString(PropsUtil.get("virtual.hosts.default.site.name"))) ?
																			"/guest" : "";

			String layoutURL = portalURL + context + virtualHostSiteName + layout.getFriendlyURL();
			Field url = new Field("url");
			url.setValue(layoutURL);
			document.add(url);
			if (_log.isDebugEnabled()) {
				_log.debug("Field 'url' with value=" + layoutURL + " added to layout=" + layout.toString());
			}
		}
	}

	@Override
	public void postProcessFullQuery(BooleanQuery fullQuery, SearchContext searchContext)
									throws Exception {
	}

	@Override
	public void postProcessSearchQuery(
									BooleanQuery searchQuery, BooleanFilter booleanFilter, SearchContext searchContext)
									throws Exception {
	}

	@Override
	public void postProcessSummary(Summary summary, Document document, Locale locale, String snippet) {
	}

	@Reference
	LayoutLocalService _layoutLocalService;

	@Reference
	GroupLocalService _groupLocalService;

	@Reference
	CompanyLocalService _companyLocalService;

	private static final Log _log = LogFactoryUtil.getLog(LayoutIndexerPostProcessor.class);
}
