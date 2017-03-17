module.exports = function(grunt) {
	// Project configuration.
	let src = "D:\\docs\\Google Drive\\tech\\workspaces\\CodG\\src\\main\\java";
	let playerPath = src + "\\jeu\\impl\\Player.java";
	let dest = "D:\\docs\\Google Drive\\tech\\workspaces\\CodG\\sync";
	let filebuilder = "java -jar ..\\..\\..\\target\\filebuilder-0.0.1-SNAPSHOT.jar ";
	
	grunt.log.writeln("FB : ");
	grunt.log.writeln(filebuilder + ' "' + playerPath + '" "' + dest+'"');

	grunt.initConfig({
		exec: {
			importManwe : {
				//cmd:filebuilder + ' "' + playerPath + '" "' + dest+'"',
				cmd:' cd ..',
			},
			compile : {
				cmd:'javac "' +dest + '\\Player.java" -d "'+dest+'\\compiled"',
			}
		},
		watchChokidar: {
			options: {
			},
			files: [src+'/**/*.java'],
			tasks: ['exec:importManwe'],
			//, 'exec:compile'],
		},
	});

	grunt.loadNpmTasks('grunt-contrib-watch-chokidar');
	grunt.loadNpmTasks('grunt-exec');
	
	grunt.registerTask('watch', ['watchChokidar']);

	};