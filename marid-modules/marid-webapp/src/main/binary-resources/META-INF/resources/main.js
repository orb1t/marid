/*-
 * #%L
 * marid-site
 * %%
 * Copyright (C) 2012 - 2018 MARID software development group
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * #L%
 */

function marid_load_main() {
    if (webix.CustomScroll && !webix.env.touch) {
        webix.CustomScroll.init();
    }

    webix.ui({
        rows: [
            {
                view: "tabview",
                cells: [
                    {
                        header: "Profiles",
                        body: {
                            template: "X"
                        }
                    },
                    {
                        header: "Beans",
                        body: {
                            template: "Y"
                        }
                    }
                ]
            },
            {
                template: "Status: all data is saved",
                height: 30
            }
        ]
    });

    webix.ui.fullScreen();
}

function marid_main() {
    webix.ready(() => marid_load_main());
}
