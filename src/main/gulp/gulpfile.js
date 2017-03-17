var gulp = require('gulp');
var exec = require('child_process').exec;
var gutil = require('gulp-util');

var src = "D:\\docs\\Google Drive\\tech\\workspaces\\CodG\\src\\main\\java";
var playerPath = src + "\\jeu\\impl\\Player.java";
var dest = "D:\\docs\\Google Drive\\tech\\workspaces\\CodG\\sync";
var filebuilder = "java -jar ..\\..\\..\\..\\target\\filebuilder-0.0.1-SNAPSHOT.jar ";

gulp.task('default', function () {
   gulp.watch(src+'/**/*.java', ['concat', 'compile']);
});

gulp.task('concat', function() {
	exec(filebuilder + ' "' + playerPath + '" "' + dest+'"', function cb(err, stdout, stderr) {
		gutil.log(stdout); // outputs the normal messages
        gutil.log(stderr); // outputs the error messages
        return 0; // makes gulp continue even if the command failed
	}	
	);
});

gulp.task('compile', function() {
	exec('javac "' +dest + '\\Player.java" -d "'+dest+'\\compiled"', function cb(err, stdout, stderr) {
		gutil.log(stdout); // outputs the normal messages
        gutil.log(stderr); // outputs the error messages
        return 0; // makes gulp continue even if the command failed
	}	
	);
});