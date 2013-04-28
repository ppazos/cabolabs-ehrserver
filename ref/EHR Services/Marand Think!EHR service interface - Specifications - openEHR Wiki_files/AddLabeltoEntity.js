
// Provide a default path to dwr.engine
if (dwr == null) var dwr = {};
if (dwr.engine == null) dwr.engine = {};
if (DWREngine == null) var DWREngine = dwr.engine;

if (AddLabelToEntity == null) var AddLabelToEntity = {};
AddLabelToEntity._path = '/wiki/dwr';
AddLabelToEntity.addLabel = function(p0, p1, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'addLabel', p0, p1, callback);
}
AddLabelToEntity.isPermitted = function(p0, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'isPermitted', p0, callback);
}
AddLabelToEntity.setPermissionManager = function(p0, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'setPermissionManager', p0, callback);
}
AddLabelToEntity.setLabelManager = function(p0, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'setLabelManager', p0, callback);
}
AddLabelToEntity.setPageManager = function(p0, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'setPageManager', p0, callback);
}
AddLabelToEntity.setLabelsService = function(p0, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'setLabelsService', p0, callback);
}
AddLabelToEntity.addFavourite = function(p0, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'addFavourite', p0, callback);
}
AddLabelToEntity.getText = function(p0, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'getText', p0, callback);
}
AddLabelToEntity.getText = function(p0, p1, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'getText', p0, p1, callback);
}
AddLabelToEntity.getText = function(p0, p1, callback) {
  dwr.engine._execute(AddLabelToEntity._path, 'AddLabelToEntity', 'getText', p0, p1, callback);
}
