
// Provide a default path to dwr.engine
if (dwr == null) var dwr = {};
if (dwr.engine == null) dwr.engine = {};
if (DWREngine == null) var DWREngine = dwr.engine;

if (RemoveLabelFromEntity == null) var RemoveLabelFromEntity = {};
RemoveLabelFromEntity._path = '/wiki/dwr';
RemoveLabelFromEntity.isPermitted = function(p0, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'isPermitted', p0, callback);
}
RemoveLabelFromEntity.setPermissionManager = function(p0, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'setPermissionManager', p0, callback);
}
RemoveLabelFromEntity.setLabelManager = function(p0, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'setLabelManager', p0, callback);
}
RemoveLabelFromEntity.setPageManager = function(p0, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'setPageManager', p0, callback);
}
RemoveLabelFromEntity.removeLabel = function(p0, p1, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'removeLabel', p0, p1, callback);
}
RemoveLabelFromEntity.isPersonalLabel = function(p0, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'isPersonalLabel', p0, callback);
}
RemoveLabelFromEntity.setLabelsService = function(p0, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'setLabelsService', p0, callback);
}
RemoveLabelFromEntity.removeFavourite = function(p0, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'removeFavourite', p0, callback);
}
RemoveLabelFromEntity.getText = function(p0, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'getText', p0, callback);
}
RemoveLabelFromEntity.getText = function(p0, p1, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'getText', p0, p1, callback);
}
RemoveLabelFromEntity.getText = function(p0, p1, callback) {
  dwr.engine._execute(RemoveLabelFromEntity._path, 'RemoveLabelFromEntity', 'getText', p0, p1, callback);
}
