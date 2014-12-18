/*
 * Copyright (C) 2014 Dmitry Ovchinnikov
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

package web

import groovy.xml.MarkupBuilder

def menuItem(MarkupBuilder builder, id, icon, label) {
    builder.li {
        a(href: "#${id}", id: "${id}-link", class: "skel-layers-ignoreHref") {
            span(class: "icon fa-${icon}", label)
        }
    }
}

def icon(MarkupBuilder builder, url, icon, label) {
    builder.li {
        a(href: url, class: "icon fa-${icon}") {
            span(class: "label", label)
        }
    }
}

def sectionHeader(MarkupBuilder builder, id, label, closure) {
    builder.section(id: id, class: "four") {
        div(class: "container") {
            header {
                h2(label)
            }
        }
    }
    builder.section(id: "${id}-contents", class: "two") {
        div(class: "container", closure)
    }
}

web({ f, e ->
    e.responseHeaders.set("Content-Type", "application/xhtml+xml; charset=UTF-8");
    e.sendResponseHeaders("ok", 0);
    e.withMarkupBuilder({ b ->
        b.html(xmlns: "http://www.w3.org/1999/xhtml") {
            head {
                script(src: "js/jquery.min.js")
                script(src: "js/jquery.scrolly.min.js")
                script(src: "js/jquery.scrollzer.min.js")
                script(src: "js/skel.min.js")
                script(src: "js/skel-layers.min.js")
                script(src: "js/init.js")
                link(rel: "stylesheet", href: "/marid.css")
            }
            body {
                div(id: "header", class: "skel-layers-fixed") {
                    div(class: "top") {
                        div(id: "logo") {
                            span(class: "image avatar48") {
                                img(src: "/marid.png")
                            }
                            h1(id: "title", "Marid")
                            p("Free Data Acquisition Software")
                        }
                        nav(id: "nav") {
                            ul {
                                menuItem(b, "top", "home", "Home");
                                menuItem(b, "demo", "th", "Demo")
                                menuItem(b, "doc", "book", "Documentation")
                                menuItem(b, "download", "download", "Download")
                                menuItem(b, "contact", "envelope", "Contact")
                            }
                        }
                    }
                    div(class: "bottom") {
                        ul(class: "icons") {
                            icon(b, "#", "github", "Github")
                            icon(b, "http://sf.net/projects/marid", "institution", "SourceForge.net")
                            icon(b, "skype:dimitrovchi?call", "skype", "Skype")
                            icon(b, "#", "youtube", "YouTube")
                            icon(b, "http://www.linuxfoundation.org", "linux", "Linux")
                            icon(b, "http://www.microsoft.com", "windows", "Windows")
                            icon(b, "http://www.apple.com", "apple", "Apple")
                        }
                    }
                }

                div(id: "main") {
                    sectionHeader(b, "top", "Project Summary", {
                        p {
                            mkp.yield("Marid is a cross-platform software intended to build complex and hierarchical data acquisition systems. ")
                            mkp.yield("The base concepts of its architecture include:")
                        }
                        ul(class: "default") {
                            li("Graphical deploy configuration builders")
                            li {
                                mkp.yield("Powerful ")
                                a(href: "http://www.groovy-lang.org", "Groovy")
                                mkp.yield(" scripting language")
                            }
                            li {
                                mkp.yield("Embedded database (")
                                a(href: "http://h2database.com", "H2")
                                mkp.yield(" , ")
                                a(href: "http://www.hsqldb.org", "HSQLDB")
                                mkp.yield(") to store data")
                            }
                            li("Cross-platform design (write once, run anywhere)")
                            li("Embedded GUI-configurable binary/ascii device protocol parser/generator")
                            li("Deploy manager to deploy firmwares to remote controllers/servers via secure channel")
                            li("Embedded web-sever")
                            li("Dynamically linked plugins available from Nexus Repository")
                            li("Remote monitoring tools")
                            li("Modular Marid IDE to manage them all")
                        }
                    })

                    sectionHeader(b, "demo", "Demo", {
                        p("This is a typical IDE screenshot:")
                        img(src: "site/screenshot1.gif", width: "100%")
                        p("The online JNLP demonstration will be available soon.")
                    })

                    sectionHeader(b, "doc", "Documentation", {
                        p {
                            mkp.yield("At now, the project documentation location is here: ")
                            a(href: "http://sf.net/p/marid/wiki/Home", "http://sf.net/p/marid/wiki/Home")
                        }
                    })

                    sectionHeader(b, "download", "Download", {
                        p {
                            mkp.yield("At now, you can download the project here: ")
                            a(href: "http://sf.net/projects/marid/files", "http://sf.net/projects/marid/files")
                        }
                    })

                    sectionHeader(b, "contact", "Contact", {
                        p {
                            table {
                                tr {
                                    td("Author:")
                                    td("Dmitry Ovchinnikov")
                                }
                            }
                        }
                    })
                }

                div(id: "footer") {
                    ul(class: "copyright") {
                        li("(c) 2014 Dmitry Ovchinnikov. All rights reserved.")
                        li {
                            mkp.yield("This site uses site templates from "); a(href: "http://html5up.net", "HTML5 UP")
                        }
                    }
                }
            }
        }
    });
})