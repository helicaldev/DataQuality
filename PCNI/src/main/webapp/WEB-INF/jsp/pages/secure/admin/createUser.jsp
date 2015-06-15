<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>


<c:if test="${not empty message}">
	<div id="successMessage" align="center" style="background:#99FF99;">
	<span id="sucsess" style="font-weight:bold;"><c:out value="${message}"/></span>
	</div>
	</c:if>
<c:if test="${not empty error}">
	<div id="errorMessage" align="center" style="background:#ffdddd;">
	<span id="error" style="font-weight:bold;"><c:out value="${error}"/></span>
	</div>
</c:if>
<br>
<form action="frmCreatUsers" method="POST" class="form-horizontal">
		<div class="form-group">
			<label class="col-sm-2 control-label"> User ID:</label>
			<div class="col-sm-10">
				<input name="id" class="form-control" type="text" value="<c:out value="${searchUserValue.id}"/>"/>
				<input class="form-control" name="hiddenUsrId" type="hidden" value="<c:out value="${searchUserValue.id}"/>" />
		</div>	
		</div>
		<div class="form-group">
			<label class="col-sm-2 control-label">User Name:</label>
			<div class="col-sm-10">
				<input class="form-control" name="username" type="text" value="<c:out value="${searchUserValue.username}"/>" />
			</div>
		</div>
			<div class="form-group">
				<label class="col-sm-2 control-label">Password:</label>
				<div class="col-sm-10">
					<input class="form-control" type="password" name="password"/>
				</div>
			</div>
			<div class="form-group">
				<label class="col-sm-2 control-label">Email Address:</label>
				<div class="col-sm-10">
				<input class="form-control" type="text" name="email" value="<c:out value="${searchUserValue.email}"/>"/>
				</div>
			</div>
			
			<div class="form-group">
				<label class="col-sm-2 control-label">Role:</label>
				<div class="col-sm-10">
				<select class="form-control" name="role">
						<option value="">Select Role</option>
							<option ${searchUserValue.role == "ROLE_USER" ? 'selected="selected"' : ''}>ROLE_USER</option>
							<option  ${searchUserValue.role == "ROLE_ADMIN" ? 'selected="selected"' : ''}>ROLE_ADMIN</option>
					</select>
				</div>
			</div>
			
			<div class="form-group">
			   <div class="col-sm-offset-2 col-sm-10">
			      <div class="checkbox">
			        <label class="col-sm-1 control-label">
			          <input type="checkbox" name="enable_status" <c:if test="${searchUserValue.status == 'Y'}">checked="checked"</c:if>/> Enable
			        </label>
			      </div>
			    </div>
			</div>
			<div class="form-group">
				<div class="col-sm-offset-2 col-sm-10">
					<div class="col-sm-2">
						<input type="submit" name="action" value="Add"  class="btn btn-success btn-block"/>
					</div>
					<div class="col-sm-2">
						<input type="submit" name="action" value="Edit" class="btn btn-warning btn-block" /> 
					</div>
					<div class="col-sm-2">
						<input type="submit" name="action" value="Delete" class="btn btn-danger btn-block"/> 
					</div>
					<div class="col-sm-2">
						<input type="submit" name="action" value="Search" class="btn btn-primary btn-block"/>
					</div>
				</div>
			</div>
	</form>

<br>
<div class="row">
		<div class="col-sm-8 col-sm-offset-2">
		<table class="table table-bordered table-striped">
		<thead>
		<tr>
			<th>User Name</th>
			<th>Organization</th>
		</tr>
		</thead>
		<tbody>
		<c:forEach items="${userList}" var="usr">
			<tr>
				<td>${usr}</td>
				<td></td>
			</tr>
		</c:forEach>
		</tbody>
	</table>
		</div>
	</div>

	