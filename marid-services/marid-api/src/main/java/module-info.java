module marid.api {

  requires static java.annotation;
  requires static marid.runtime;

  exports org.marid.db.dao;
  exports org.marid.db.data;
  exports org.marid.db.generator;

  exports org.marid.proto;
  exports org.marid.proto.io;
}