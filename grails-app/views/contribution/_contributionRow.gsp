<%-- usado desde ehr/_ehrContributions.gsp --%>
<tr><!-- contribution data -->
  <td>${contribution.audit.timeCommitted}</td>
  <td>${contribution.audit.committer.name}</td>
</tr>
<tr>
  <td colspan="2">
    <table class="table table-striped table-bordered table-hover" style="margin-bottom:0px;"><!-- contribution versions data -->
      <tr>
        <th>uid</th>
        <th>creation date</th>
        <th>type</th>
        <th>change type</th>
        <th></th>
      </tr>
      <g:each in="${contribution.versions}" var="version">
        <g:render template="../version/versionRow" model="[version:version]"/>
      </g:each>
    </table>
  </td>
</tr>
