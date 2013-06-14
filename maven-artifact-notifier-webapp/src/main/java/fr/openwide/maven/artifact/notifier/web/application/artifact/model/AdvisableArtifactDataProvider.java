package fr.openwide.maven.artifact.notifier.web.application.artifact.model;

import java.util.List;

import org.apache.lucene.search.SortField;
import org.apache.wicket.injection.Injector;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.google.common.collect.Lists;

import fr.openwide.core.wicket.more.markup.repeater.data.LoadableDetachableDataProvider;
import fr.openwide.maven.artifact.notifier.core.business.artifact.model.Artifact;
import fr.openwide.maven.artifact.notifier.core.business.artifact.service.IArtifactService;
import fr.openwide.maven.artifact.notifier.core.config.application.MavenArtifactNotifierConfigurer;
import fr.openwide.maven.artifact.notifier.core.util.binding.Binding;

public class AdvisableArtifactDataProvider extends LoadableDetachableDataProvider<Artifact> {

	private static final long serialVersionUID = -6735682878632622767L;
	
	private static final Logger LOGGER = LoggerFactory.getLogger(AdvisableArtifactDataProvider.class);

	@SpringBean
	private IArtifactService artifactService;
	
	@SpringBean
	private MavenArtifactNotifierConfigurer configurer;
	
	private IModel<String> globalSearchModel;
	
	private IModel<String> searchGroupModel;
	
	private IModel<String> searchArtifactModel;
	
	public AdvisableArtifactDataProvider(IModel<String> globalSearchModel, IModel<String> searchGroupModel, IModel<String> searchArtifactModel) {
		Injector.get().inject(this);
		
		if (globalSearchModel == null || searchGroupModel == null || searchArtifactModel == null) {
			throw new IllegalArgumentException("Null models are not supported.");
		}
		
		this.globalSearchModel = globalSearchModel;
		this.searchGroupModel = searchGroupModel;
		this.searchArtifactModel = searchArtifactModel;
	}
	
	@Override
	public IModel<Artifact> model(Artifact artifact) {
		return new Model<Artifact>(artifact);
	}
	
	@Override
	protected List<Artifact> loadList(long first, long count) {
		// TODO : Appliquer des regex pour verifier l'entree utilisateur
		try {
			if (StringUtils.hasText(globalSearchModel.getObject())) {
				SortField sortField = new SortField(Binding.artifact().followersCount().getPath(), SortField.LONG, true);
				return artifactService.search(globalSearchModel.getObject(), Lists.newArrayList(sortField),
						configurer.getAdvisableArtifactItemsLimit(), (int) first);
			}
		} catch (Exception e) {
			LOGGER.error("Unable to retrieve the local artifacts for search: '" + globalSearchModel.getObject() + "'", e);
		}
		return Lists.newArrayListWithExpectedSize(0);
	}

	@Override
	protected long loadSize() {
		try {
			if (StringUtils.hasText(globalSearchModel.getObject())) {
				return artifactService.countSearch(globalSearchModel.getObject());
			}
		} catch (Exception e) {
			LOGGER.error("Unable to retrieve the local artifacts for search: '" + globalSearchModel.getObject() + "'", e);
		}
		return 0;
	}
	
	@Override
	public void detach() {
		super.detach();
		if (globalSearchModel != null) {
			globalSearchModel.detach();
		}
		if (searchGroupModel != null) {
			searchGroupModel.detach();
		}
		if (searchArtifactModel != null) {
			searchArtifactModel.detach();
		}
	}
}
