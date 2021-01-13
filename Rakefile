# coding: utf-8
task :default => :package

JAR_FILE = "target/sample-a-0.0.1-SNAPSHOT-jar-with-dependencies.jar"
SRC_FILES = FileList["**/*.kt"]

desc "Make package"
task :package => JAR_FILE
file JAR_FILE => SRC_FILES do |t|
  sh "./build.sh package"
end
