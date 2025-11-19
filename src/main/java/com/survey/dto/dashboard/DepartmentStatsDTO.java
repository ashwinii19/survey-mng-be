package com.survey.dto.dashboard;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentStatsDTO {

	private Long departmentId;
	private String departmentName;
	private int totalEmployees;
	private int submitted;
	private int pending;
	private double responseRate;
}
