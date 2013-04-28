
// Provide a default path to dwr.engine
if (dwr == null) var dwr = {};
if (dwr.engine == null) dwr.engine = {};
if (DWREngine == null) var DWREngine = dwr.engine;

if (SuggestedLabelsForEntity == null) var SuggestedLabelsForEntity = {};
SuggestedLabelsForEntity._path = '/wiki/dwr';
SuggestedLabelsForEntity.setLabelManager = function(p0, callback) {
  dwr.engine._execute(SuggestedLabelsForEntity._path, 'SuggestedLabelsForEntity', 'setLabelManager', p0, callback);
}
SuggestedLabelsForEntity.setPageManager = function(p0, callback) {
  dwr.engine._execute(SuggestedLabelsForEntity._path, 'SuggestedLabelsForEntity', 'setPageManager', p0, callback);
}
SuggestedLabelsForEntity.viewLabels = function(p0, callback) {
  dwr.engine._execute(SuggestedLabelsForEntity._path, 'SuggestedLabelsForEntity', 'viewLabels', p0, callback);
}
SuggestedLabelsForEntity.getText = function(p0, callback) {
  dwr.engine._execute(SuggestedLabelsForEntity._path, 'SuggestedLabelsForEntity', 'getText', p0, callback);
}
SuggestedLabelsForEntity.getText = function(p0, p1, callback) {
  dwr.engine._execute(SuggestedLabelsForEntity._path, 'SuggestedLabelsForEntity', 'getText', p0, p1, callback);
}
SuggestedLabelsForEntity.getText = function(p0, p1, callback) {
  dwr.engine._execute(SuggestedLabelsForEntity._path, 'SuggestedLabelsForEntity', 'getText', p0, p1, callback);
}
