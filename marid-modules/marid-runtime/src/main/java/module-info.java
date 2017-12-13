module marid.runtime {

  requires java.annotation;
  requires transitive marid.util;

  exports org.marid.beans;
  exports org.marid.expression.generic;
  exports org.marid.expression.runtime;
  exports org.marid.expression.xml;
  exports org.marid.runtime;
  exports org.marid.runtime.context;
  exports org.marid.runtime.event;
  exports org.marid.runtime.exception;
  exports org.marid.runtime.lambda;
  exports org.marid.types;
}