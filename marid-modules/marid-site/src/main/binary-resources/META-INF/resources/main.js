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

    function random_data() {
        let data = [];
        for (let i = 0; i < 10; i++)
            data.push({ind: i + 1, sales: Math.round(Math.random() * 5000)});
        return data;
    }

    function cell(month) {
        return {
            minWidth: 300,
            height: 400,
            rows: [
                {template: "Report: " + month + " 2016", type: "header"},
                {
                    view: "chart", type: "line", preset: "plot",
                    value: "#sales#", xAxis: {template: "#ind#"}, yAxis: {},
                    data: random_data()
                }
            ]
        };
    }

    let flex = {
        margin: 10, padding: 0, type: "wide",
        view: "flexlayout",
        cols: [
            cell("January"),
            cell("February"),
            cell("March"),
            cell("April"),
            cell("May"),
            cell("June"),
            cell("July"),
            cell("August"),
            cell("September"),
            cell("October"),
            cell("November"),
            cell("December")
        ]
    };

    let app = webix.ui({
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
