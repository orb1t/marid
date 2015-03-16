/*
 * Copyright (C) 2015 Dmitry Ovchinnikov
 * Marid, the free data acquisition and visualization software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.marid.service.proto;

import org.marid.service.proto.pb.PbService;
import org.marid.service.proto.pb.PbServiceConfiguration;
import org.marid.service.proto.pp.PpService;
import org.marid.service.proto.pp.PpServiceConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static org.marid.groovy.GroovyRuntime.newInstance;

/**
 * @author Dmitry Ovchinnikov
 */
@Configuration
public class ProtoTestConfiguration {

    @Bean
    public PpServiceConfiguration ppServiceConfiguration() throws Exception {
        return newInstance(PpServiceConfiguration.class, getClass().getResource("/PpContext.groovy"));
    }

    @Bean
    @DependsOn("pbService")
    public PpService ppService() throws Exception {
        return new PpService(ppServiceConfiguration());
    }

    @Bean
    public PbServiceConfiguration pbServiceConfiguration() throws Exception {
        return newInstance(PbServiceConfiguration.class, getClass().getResource("/PbContext.groovy"));
    }

    @Bean
    public PbService pbService() throws Exception {
        return new PbService(pbServiceConfiguration());
    }
}
