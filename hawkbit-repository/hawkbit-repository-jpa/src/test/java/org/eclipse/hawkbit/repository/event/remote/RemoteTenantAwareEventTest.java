/**
 * Copyright (c) 2015 Bosch Software Innovations GmbH and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.hawkbit.repository.event.remote;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.eclipse.hawkbit.repository.jpa.model.JpaAction;
import org.eclipse.hawkbit.repository.model.Action;
import org.eclipse.hawkbit.repository.model.Action.ActionType;
import org.eclipse.hawkbit.repository.model.Action.Status;
import org.eclipse.hawkbit.repository.model.ActionProperties;
import org.eclipse.hawkbit.repository.model.DistributionSet;
import org.eclipse.hawkbit.repository.model.Target;
import org.junit.Test;

import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Story;

@Feature("Component Tests - Repository")
@Story("RemoteTenantAwareEvent Tests")
public class RemoteTenantAwareEventTest extends AbstractRemoteEventTest {

    @Test
    @Description("Verifies that the download progress reloading by remote events works")
    public void reloadDownloadProgessByRemoteEvent() {
        final DownloadProgressEvent downloadProgressEvent = new DownloadProgressEvent("DEFAULT", 1L, 3L, "Node");

        DownloadProgressEvent remoteEvent = (DownloadProgressEvent) createProtoStuffEvent(downloadProgressEvent);
        assertThat(downloadProgressEvent).isEqualTo(remoteEvent);

        remoteEvent = (DownloadProgressEvent) createJacksonEvent(downloadProgressEvent);
        assertThat(downloadProgressEvent).isEqualTo(remoteEvent);
    }

    @Test
    @Description("Verifies that target assignment event works")
    public void testTargetAssignDistributionSetEvent() {
        final DistributionSet dsA = testdataFactory.createDistributionSet("");
        final JpaAction generateAction = new JpaAction();
        generateAction.setActionType(ActionType.FORCED);
        final Target target = testdataFactory.createTarget("Test");
        generateAction.setTarget(target);
        generateAction.setDistributionSet(dsA);
        generateAction.setStatus(Status.RUNNING);
        final Action action = actionRepository.save(generateAction);

        final TargetAssignDistributionSetEvent assignmentEvent = new TargetAssignDistributionSetEvent(
                action.getTenant(), dsA.getId(), Arrays.asList(action), serviceMatcher.getServiceId(),
                action.isMaintenanceWindowAvailable());

        TargetAssignDistributionSetEvent underTest = (TargetAssignDistributionSetEvent) createProtoStuffEvent(
                assignmentEvent);
        assertTargetAssignDistributionSetEvent(action, underTest);

        underTest = (TargetAssignDistributionSetEvent) createJacksonEvent(assignmentEvent);
        assertTargetAssignDistributionSetEvent(action, underTest);
    }

    private void assertTargetAssignDistributionSetEvent(final Action action,
            final TargetAssignDistributionSetEvent underTest) {

        assertThat(underTest.getActions().size()).isEqualTo(1);
        ActionProperties actionProperties = underTest.getActions().get(action.getTarget().getControllerId());
        assertThat(actionProperties).isNotNull();
        assertThat(actionProperties).isEqualToComparingFieldByField(new ActionProperties(action));
        assertThat(underTest.getDistributionSetId()).isEqualTo(action.getDistributionSet().getId());
    }

}
