const fs = require('fs').promises;
const path = require('path');

(async function(){
  const src = process.cwd();
  const baseName = path.basename(src);
  const dest = path.join(path.dirname(src), baseName + '-public');
  const exclude = new Set(['.git','.idea','node_modules','backend/target','backend/bin','target','dist']);

  async function removeIfExists(p){
    try{ await fs.rm(p,{recursive:true,force:true}); }catch(e){}
  }
  await removeIfExists(dest);
  await fs.mkdir(dest,{recursive:true});

  async function copyRecursive(srcDir, outDir){
    const entries = await fs.readdir(srcDir,{withFileTypes:true});
    for(const ent of entries){
      if(exclude.has(ent.name)) continue;
      const srcPath = path.join(srcDir, ent.name);
      const destPath = path.join(outDir, ent.name);
      if(ent.isDirectory()){
        await fs.mkdir(destPath,{recursive:true});
        await copyRecursive(srcPath,destPath);
      } else if(ent.isFile()){
        // skip binaries
        if(/\.(class|jar)$/.test(ent.name)) continue;
        const data = await fs.readFile(srcPath,'utf8');
        await fs.writeFile(destPath,data,'utf8');
      }
    }
  }

  await copyRecursive(src,dest);

  // write .gitignore
  const gitignore = `# Build\ntarget/\nbackend/target/\ndist/\nnode_modules/\n\n# IDE\n.idea/\n.vscode/\n*.iml\n\n# Compiled\n*.class\n*.jar\n\n# OS\n.DS_Store\nThumbs.db\n\n# Secrets\n.env\n*.jks\n*.keystore\n*.p12\n`;
  await fs.writeFile(path.join(dest,'.gitignore'),gitignore,'utf8');

  // sanitize application.properties
  const appProps = path.join(dest,'backend','src','main','resources','application.properties');
  try{
    let t = await fs.readFile(appProps,'utf8');
    t = t.replace(/spring\.datasource\.username=.*$/m, 'spring.datasource.username=${JDBC_DATABASE_USERNAME:}');
    t = t.replace(/spring\.datasource\.password=.*$/m, 'spring.datasource.password=${JDBC_DATABASE_PASSWORD:}');
    t = t.replace(/spring\.datasource\.url=.*$/m, 'spring.datasource.url=${JDBC_DATABASE_URL:jdbc:mysql://localhost:3306/unicourse?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}');
    await fs.writeFile(appProps,t,'utf8');
    console.log('脱敏 application.properties');
  }catch(e){ console.log('未找到 application.properties，已跳过。'); }

  // sanitize WORKFLOW.md
  const wf = path.join(dest,'WORKFLOW.md');
  try{
    let t = await fs.readFile(wf,'utf8');
    t = t.replace(/unicourse_user/g,'<DB_USERNAME>');
    t = t.replace(/change_me/g,'<DB_PASSWORD>');
    await fs.writeFile(wf,t,'utf8');
    console.log('脱敏 WORKFLOW.md');
  }catch(e){ }

  console.log('公共副本已创建：', dest);
})();
