/*-
 * #%L
 * marid-ide
 * %%
 * Copyright (C) 2012 - 2017 MARID software development group
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

package org.marid.ide.maven;

import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.stage.Modality;
import org.apache.lucene.search.BooleanQuery;
import org.apache.maven.index.Indexer;
import org.apache.maven.index.expr.StringSearchExpression;
import org.controlsfx.validation.ValidationSupport;
import org.marid.Ide;
import org.marid.ide.model.MavenArtifact;
import org.marid.jfx.panes.GenericGridPane;
import org.marid.spring.annotation.PrototypeComponent;
import org.springframework.beans.factory.annotation.Autowired;

import static javafx.scene.control.ButtonType.APPLY;
import static javafx.scene.control.ButtonType.CANCEL;
import static org.apache.commons.lang3.StringUtils.isNotBlank;
import static org.apache.lucene.search.BooleanClause.Occur.SHOULD;
import static org.apache.maven.index.MAVEN.ARTIFACT_ID;
import static org.apache.maven.index.MAVEN.GROUP_ID;
import static org.controlsfx.validation.Validator.createEmptyValidator;
import static org.marid.l10n.L10n.m;
import static org.marid.l10n.L10n.s;

@PrototypeComponent
public class MavenArtifactFinder extends Dialog<MavenArtifact> {

    private final ValidationSupport v = new ValidationSupport();

    private final ComboBox<String> groupIdBox = new ComboBox<>();
    private final ComboBox<String> artifactIdBox = new ComboBox<>();
    private final ComboBox<String> versionBox = new ComboBox<>();

    @Autowired
    public MavenArtifactFinder(MavenRepositoryManager repositoryManager) {
        groupIdBox.setEditable(true);
        artifactIdBox.setEditable(true);
        versionBox.setEditable(true);

        setOnShowing(event -> Platform.runLater(() -> {
            v.registerValidator(groupIdBox, true, createEmptyValidator(m("Empty groupId")));
            v.registerValidator(artifactIdBox, true, createEmptyValidator(m("Empty artifactId")));
            v.registerValidator(versionBox, true, createEmptyValidator(m("Empty version")));
        }));

        initModality(Modality.APPLICATION_MODAL);
        initOwner(Ide.primaryStage);

        setTitle(s("Find a maven artifact"));
        setHeaderText(s("Enter maven artifact coordinates") + ": ");

        final GenericGridPane pane = new GenericGridPane();
        pane.addControl("groupId", () -> groupIdBox);
        pane.addControl("artifactId", () -> artifactIdBox);
        pane.addControl("version", () -> versionBox);

        groupIdBox.getItems().add("org.marid");
        artifactIdBox.setOnShowing(event -> {
            if (isNotBlank(getGroupId())) {
                final String[] artifacts = repositoryManager.getMaridArtifacts(this::artifactQuery)
                        .map(MavenArtifact::getArtifactId)
                        .distinct()
                        .toArray(String[]::new);
                artifactIdBox.getItems().setAll(artifacts);
            }
        });
        versionBox.setOnShowing(event -> {
            if (isNotBlank(getGroupId()) && isNotBlank(getArtifactId())) {
                final String[] versions = repositoryManager.getMaridArtifacts(this::versionQuery)
                        .map(MavenArtifact::getVersion)
                        .distinct()
                        .toArray(String[]::new);
                versionBox.getItems().setAll(versions);
            }
        });

        getDialogPane().setContent(pane);
        getDialogPane().getButtonTypes().setAll(CANCEL, APPLY);

        setResultConverter(t -> {
            v.initInitialDecoration();
            switch (t.getButtonData()) {
                case APPLY:
                    return new MavenArtifact(getGroupId(), getArtifactId(), getVersion());
                default:
                    return null;
            }
        });

        setOnCloseRequest(event -> {
            if (v.isInvalid() && getResult() != null) {
                v.initInitialDecoration();
                setResult(null);
                event.consume();
            }
        });

        getDialogPane().setPrefSize(800, 600);
    }

    private String getGroupId() {
        return groupIdBox.getSelectionModel().getSelectedItem();
    }

    private String getArtifactId() {
        return artifactIdBox.getSelectionModel().getSelectedItem();
    }

    private String getVersion() {
        return versionBox.getSelectionModel().getSelectedItem();
    }

    private BooleanQuery artifactQuery(Indexer indexer) {
        final BooleanQuery query = new BooleanQuery();
        query.setMinimumNumberShouldMatch(1);
        query.add(indexer.constructQuery(GROUP_ID, new StringSearchExpression(getGroupId())), SHOULD);
        return query;
    }

    private BooleanQuery versionQuery(Indexer indexer) {
        final BooleanQuery query = new BooleanQuery();
        query.setMinimumNumberShouldMatch(2);
        query.add(indexer.constructQuery(GROUP_ID, new StringSearchExpression(getGroupId())), SHOULD);
        query.add(indexer.constructQuery(ARTIFACT_ID, new StringSearchExpression(getArtifactId())), SHOULD);
        return query;
    }
}
